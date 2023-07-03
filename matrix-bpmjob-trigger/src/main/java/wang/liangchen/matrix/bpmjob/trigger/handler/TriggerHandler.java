package wang.liangchen.matrix.bpmjob.trigger.handler;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.domain.trigger.TriggerManager;
import wang.liangchen.matrix.bpmjob.domain.trigger.TriggerTime;
import wang.liangchen.matrix.bpmjob.domain.trigger.Wal;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissedStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.TriggerState;
import wang.liangchen.matrix.framework.commons.collection.CollectionUtil;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.exception.MatrixErrorException;
import wang.liangchen.matrix.framework.commons.thread.ThreadUtil;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;
import wang.liangchen.matrix.framework.data.util.TransactionUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author Liangchen.Wang
 */
@Service
public class TriggerHandler {
    private final static Logger logger = LoggerFactory.getLogger(TriggerHandler.class);
    private final TriggerManager triggerManager;
    private final TriggerProperties triggerProperties;


    private volatile boolean halted = true;
    // TriggerThread/WalThread/TaskThread
    private final CountDownLatch countDownLatch = new CountDownLatch(3);
    private final TriggerThread triggerThread = new TriggerThread();
    private final WalThread walThread = new WalThread();
    private final ScheduledThreadPoolExecutor triggerPool;


    @Inject
    public TriggerHandler(TriggerManager triggerManager, ObjectProvider<TriggerProperties> triggerPropertiesObjectProvider) {
        this.triggerManager = triggerManager;
        this.triggerProperties = triggerPropertiesObjectProvider.getIfAvailable(TriggerProperties::new);
        this.triggerPool = new ScheduledThreadPoolExecutor(triggerProperties.getThreadNumber(), new CustomizableThreadFactory("job-"));
        // start thread
        this.walThread.start();
        this.triggerThread.start();
    }

    public synchronized void start() {
        if (this.halted) {
            // register hooker
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            this.halted = false;
        }
    }

    public synchronized void shutdown() {
        if (this.halted) {
            return;
        }
        // halt all threads
        this.halted = true;
        try {
            logger.info("Waiting for threads to complete ...");
            this.countDownLatch.await();
            logger.info("Waiting for thread pool shutdown ...");
            this.triggerPool.shutdown();
            boolean shutdown = this.triggerPool.awaitTermination(1, TimeUnit.HOURS);
            logger.info("thread pool shutdown: {}", shutdown);
        } catch (Exception e) {
            logger.error("shutdown error", e);
        }
    }

    private class TriggerThread extends Thread {
        TriggerThread() {
            super("trigger-");
        }

        @Override
        public void run() {
            long sleep = 0;
            while (true) {
                /*--------------------------halted and break-------------------------------*/
                if (halted) {
                    countDownLatch.countDown();
                    logger.info("The TriggerThread is halted. Shutdown...");
                    break;
                }
                /*--------------------------need sleep become some reasons-------------------------------*/
                if (sleep > 0) {
                    ThreadUtil.INSTANCE.sleep(TimeUnit.SECONDS, sleep);
                    sleep = 0;
                }
                /*--------------------------acquire eligible triggers-------------------------------*/
                List<TriggerTime> triggerTimes;
                try {
                    triggerTimes = acquireTriggerTimes();
                    if (CollectionUtil.INSTANCE.isEmpty(triggerTimes)) {
                        sleep = 1;
                        logger.info("Triggers acquisition empty.delay '{}s' and then retry", sleep);
                        continue;
                    }
                } catch (Exception e) {
                    sleep = 5;
                    logger.error("Triggers acquisition failed.delay '{}s' and then retry", sleep);
                    continue;
                }
                /*--------------------------exclusive trigger and fire it-------------------------------*/
                for (TriggerTime triggerTime : triggerTimes) {
                    try {
                        exclusiveTrigger(triggerTime);
                    } catch (Exception e) {
                        logger.error("Trigger exclusive failure.Trigger: " + triggerTime.getTriggerId(), e);
                    }
                }
            }
        }
    }


    private List<TriggerTime> acquireTriggerTimes() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        // 未来xS之前应该触发的所有触发器
        LocalDateTime duration = now.plus(this.triggerProperties.getAcquireTriggerDuration());
        // future: triggerInstant < now+acquireTriggerDuration
        List<TriggerTime> triggerTimes = triggerManager.eligibleTriggerTimes(duration, this.triggerProperties.getBatchSize());
        // 乱序
        Collections.shuffle(triggerTimes);
        return triggerTimes;
    }

    private void exclusiveTrigger(TriggerTime triggerTime) {
        Long triggerId = triggerTime.getTriggerId();
        Trigger trigger = triggerManager.selectTrigger(triggerId, Trigger::getTriggerCron, Trigger::getMissedStrategy, Trigger::getMissedThreshold, Trigger::getState);
        if (null == trigger) {
            logger.info("Trigger '{}' doesnot exist", triggerId);
            return;
        }
        String hostLabel = this.triggerProperties.getHostLabel();
        if (!Objects.equals(TriggerState.NORMAL.key(), trigger.getState().key())) {
            logger.info("The state of trigger '{}' isn't NORMAL,skipped by Host:{}", triggerId, hostLabel);
            return;
        }

        logger.info("The trigger '{}' is desired by Host:{}", triggerId, hostLabel);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerInstant = triggerTime.getTriggerInstant();
        long delayMS = Duration.between(now, triggerInstant).toMillis();
        boolean missed = delayMS <= -trigger.getMissedThreshold() * 1000;

        // parse the next trigger instant, if missed,benchmark is now
        LocalDateTime benchmark = missed ? now : triggerInstant;
        LocalDateTime nextTriggerInstant = CronExpression.parse(trigger.getTriggerCron()).next(benchmark);

        // 在同一事务中,通过update抢占操作权和创建预写日志
        Optional<Wal> optionalWal = TransactionUtil.INSTANCE.execute(() -> {
            boolean renew = triggerManager.renewTriggerInstant(triggerId, triggerInstant, nextTriggerInstant);
            if (renew) {
                logger.info("Exclusive Trigger Success. Trigger:{}, Host:{}", triggerId, hostLabel);
                Wal innerWal = triggerManager.createWal(hostLabel, triggerId, triggerInstant, JsonField.newInstance());
                logger.info("Wal created, Wal:{},Trigger:{}, Host:{}", innerWal.getWalId(), trigger.getTriggerId(), hostLabel);
                return Optional.of(innerWal);
            }
            logger.info("Exclusive Trigger Failure. Trigger:{}, Host:{}", triggerId, hostLabel);
            return Optional.empty();
        });
        optionalWal.ifPresent(wal -> {
            // miss
            if (missed) {
                missedTrigger(trigger, wal);
                return;
            }
            // immediate & schedule
            offerDelayQueue(wal, delayMS);
        });
    }


    private void missedTrigger(Trigger trigger, Wal wal) {
        MissedStrategy missedStrategy = trigger.getMissedStrategy();
        logger.info("MissedStrategy is:{}.Wal:{},Trigger:{}, Host:{}", missedStrategy, wal.getWalId(), wal.getTriggerId(), this.triggerProperties.getHostLabel());
        switch (missedStrategy) {
            case COMPENSATE:
                offerDelayQueue(wal, 0L);
                break;
            case SKIP:
                confirmWal(wal);
                logger.info("Skip missed Wal:{}, Trigger:{}", wal.getWalId(), wal.getTriggerId());
                break;
            default:
                break;
        }
    }

    private class WalThread extends Thread {
        WalThread() {
            super("wal-");
        }

        @Override
        public void run() {
            long sleep = 0;
            while (true) {
                /*--------------------------halted and break-------------------------------*/
                if (halted) {
                    countDownLatch.countDown();
                    logger.info("The WalThread is halted. Shutdown...");
                    break;
                }
                /*--------------------------need sleep become some reasons-------------------------------*/
                if (sleep > 0) {
                    ThreadUtil.INSTANCE.sleep(TimeUnit.SECONDS, sleep);
                    sleep = 0;
                }
                /*--------------------------acquire missed wals-------------------------------*/
                List<Long> walIds;
                try {
                    walIds = acquireEligibleWals();
                    if (CollectionUtil.INSTANCE.isEmpty(walIds)) {
                        sleep = 1;
                        logger.info("Wals acquisition empty.delay '{}s' and then retry", sleep);
                        continue;
                    }
                } catch (Exception e) {
                    sleep = 5;
                    logger.error("Wals acquisition failed.delay '{}s' and then retry", sleep);
                    continue;
                }
                /*--------------------------redo wals-------------------------------*/
                for (Long walId : walIds) {
                    try {
                        exclusiveAndRedoWal(walId);
                    } catch (Exception e) {
                        logger.error("redoWal failed.Wal: " + walId, e);
                    }
                }
            }
        }
    }

    private List<Long> acquireEligibleWals() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        // past: triggerInstant < now - 2s
        LocalDateTime duration = now.minusSeconds(2);
        List<Long> walIds = triggerManager.eligibleWalIds(duration, this.triggerProperties.getBatchSize());
        // 乱序
        Collections.shuffle(walIds);
        return walIds;
    }

    private void exclusiveAndRedoWal(Long walId) {
        Wal wal = triggerManager.selectWal(walId);
        String hostLabel = this.triggerProperties.getHostLabel();
        if (null == wal) {
            logger.info("Wal '{}' doesn't exist,Maybe It has been confirmed.skipped by Host:{}", walId, hostLabel);
            return;
        }
        logger.info("Wal '{}' is desired by Host:{} and offer to queue", walId, hostLabel);
        offerDelayQueue(wal, 0L);
    }

    private void offerDelayQueue(Wal wal, long delayMS) {
        String hostLabel = this.triggerProperties.getHostLabel();
        if (delayMS < 100) {
            logger.info("immediate tasks. delayMS:{},Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), hostLabel);
            triggerPool.execute(() -> confirmWalAndCreateTask(wal));
            return;
        }

        logger.info("asynchronous tasks. delayMS:{}, Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), hostLabel);
        triggerPool.schedule(() -> confirmWalAndCreateTask(wal), delayMS, TimeUnit.MILLISECONDS);
    }

    private void confirmWalAndCreateTask(Wal wal) {
        Long walId = wal.getWalId();
        // 同一事务确认(删除)Wal和创建任务
        TransactionUtil.INSTANCE.execute(() -> {
            int rows = confirmWal(wal);
            // redo 时,其它线程已经确认这个wal并且创建任务
            if (0 == rows) {
                logger.info("Confirm Wal failed.Maybe It has been confirmed.Wal: {}", walId);
                return;
            }
            try {
                // 状态正常才创建任务
                Optional<Trigger> optionalTrigger = triggerManager.state(wal.getTriggerId());
                optionalTrigger.filter(trigger -> Objects.equals(TriggerState.NORMAL.key(), trigger.getState().key()))
                        .ifPresent(trigger -> {
                            // TODO
                        });
            } catch (Exception e) {
                logger.error("create task error", e);
                throw new MatrixErrorException(e);
            }
            logger.info("Task created.Task: {}", walId);
        });
    }

    private int confirmWal(Wal wal) {
        return triggerManager.deleteWal(wal.getWalId());
    }

    private class TaskThread extends Thread {
        TaskThread() {
            super("task-");
        }

        @Override
        public void run() {
            long sleep = 0;
            while (true) {
                /*--------------------------halted and break-------------------------------*/
                if (halted) {
                    countDownLatch.countDown();
                    logger.info("The TaskThread is halted. Shutdown...");
                    break;
                }
                /*--------------------------need sleep become some reasons-------------------------------*/
                if (sleep > 0) {
                    ThreadUtil.INSTANCE.sleep(TimeUnit.SECONDS, sleep);
                    sleep = 0;
                }
                /*--------------------------acquire tasks-------------------------------*/
                // TODO
                List<Long> taskIds = new ArrayList<>();
                try {
                    if (CollectionUtil.INSTANCE.isEmpty(taskIds)) {
                        sleep = 1;
                        logger.info("Tasks acquisition empty.delay '{}s' and then retry", sleep);
                        continue;
                    }
                } catch (Exception e) {
                    sleep = 5;
                    logger.error("Tasks acquisition failed.delay '{}s' and then retry", sleep);
                    continue;
                }
            }
        }
    }

}