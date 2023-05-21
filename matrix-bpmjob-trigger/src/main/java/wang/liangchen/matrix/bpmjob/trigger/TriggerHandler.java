package wang.liangchen.matrix.bpmjob.trigger;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.task.TaskManager;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author Liangchen.Wang
 */
@Service
public class TriggerHandler implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(TriggerHandler.class);

    private final TriggerManager triggerManager;
    private final TaskManager taskManager;
    private final String hostLabel;
    private final Short batchSize;
    private Duration acquireTriggerDuration;
    private Duration missedThreshold;


    private volatile boolean halted = false;
    private final CountDownLatch countDownLatch = new CountDownLatch(2);
    private final TriggerThread triggerThread = new TriggerThread();
    private final WalThread walThread = new WalThread();
    private final ScheduledThreadPoolExecutor triggerPool;

    @Inject
    public TriggerHandler(TriggerManager triggerManager, TaskManager taskManager, ObjectProvider<TriggerProperties> triggerPropertiesObjectProvider) {
        this.triggerManager = triggerManager;
        this.taskManager = taskManager;
        TriggerProperties triggerProperties = triggerPropertiesObjectProvider.getIfAvailable(TriggerProperties::new);
        this.triggerPool = new ScheduledThreadPoolExecutor(triggerProperties.getThreadNumber(), new CustomizableThreadFactory("job-"));
        this.hostLabel = triggerProperties.getHostLabel();
        this.batchSize = triggerProperties.getBatchSize();
        this.acquireTriggerDuration = triggerProperties.getAcquireTriggerDuration();
        this.missedThreshold = triggerProperties.getMissedThreshold();

        // start thread
        this.walThread.start();
        this.triggerThread.start();
    }

    @Override
    public void destroy() throws Exception {
        // halt all threads
        this.halted = true;
        logger.info("Waiting for task creation to complete ...");
        this.countDownLatch.await();
        logger.info("Waiting for thread pool shutdown ...");
        this.triggerPool.shutdown();
        boolean shutdown = this.triggerPool.awaitTermination(1, TimeUnit.MINUTES);
        logger.info("thread pool shutdown: {}", shutdown);
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
        List<Long> walIds = triggerManager.eligibleWalIds(duration, this.batchSize);
        // 乱序
        Collections.shuffle(walIds);
        return walIds;
    }

    private void exclusiveAndRedoWal(Long walId) {
        Wal wal = triggerManager.selectWal(walId);
        if (null == wal) {
            logger.info("Wal '{}' doesn't exist,Maybe It has been confirmed.skipped by Host:{}", walId, hostLabel);
            return;
        }
        logger.info("Wal '{}' is desired by Host:{} and offer to queue", walId, hostLabel);
        offerDelayQueue(wal, 0L);
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
                    logger.info("The WalThread is halted. Shutdown...");
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
        // 未来15S之前应该触发的所有触发器
        LocalDateTime duration = now.plus(this.acquireTriggerDuration);
        // future: triggerInstant < now+acquireTriggerDuration
        List<TriggerTime> triggerTimes = triggerManager.eligibleTriggerTimes(duration, batchSize);
        // 乱序
        Collections.shuffle(triggerTimes);
        return triggerTimes;
    }

    private void exclusiveTrigger(TriggerTime triggerTime) {
        Long triggerId = triggerTime.getTriggerId();
        Trigger trigger = triggerManager.selectTrigger(triggerId,
                Trigger::getTriggerId, Trigger::getTriggerGroup, Trigger::getTriggerName, Trigger::getTriggerType, Trigger::getTriggerCron,
                Trigger::getExecutorType, Trigger::getExecutorOption, Trigger::getMissedThreshold, Trigger::getMissedStrategy, Trigger::getAssignStrategy,
                Trigger::getShardingStrategy, Trigger::getShardingNumber, Trigger::getTriggerParams, Trigger::getExtendedSettings, Trigger::getTaskSettings, Trigger::getState);
        if (null == trigger) {
            return;
        }
        if (!Objects.equals(TriggerState.NORMAL.key(), trigger.getState().key())) {
            logger.info("The state of trigger '{}' isn't NORMAL,skipped by Host:{}", triggerId, hostLabel);
            return;
        }

        logger.info("The trigger '{}' is desired by Host:{}", triggerId, hostLabel);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerInstant = triggerTime.getTriggerInstant();
        long delayMS = Duration.between(now, triggerInstant).toMillis();
        boolean missed = delayMS <= -this.missedThreshold.toMillis();

        // parse the next trigger instant, if missed,benchmark is now
        LocalDateTime benchmark = missed ? now : triggerInstant;
        LocalDateTime nextTriggerInstant = CronExpression.parse(trigger.getTriggerCron()).next(benchmark);

        // 在同一事务中,通过update抢占操作权和创建预写日志
        Wal wal = TransactionUtil.INSTANCE.execute(() -> {
            boolean renew = triggerManager.renewTriggerInstant(triggerId, triggerInstant, nextTriggerInstant);
            if (renew) {
                logger.info("Exclusive Trigger Success. Trigger:{}, Host:{}", triggerId, this.hostLabel);
                Wal innerWal = triggerManager.createWal(this.hostLabel, trigger, triggerInstant, JsonField.newInstance());
                logger.info("Wal created, Wal:{},Trigger:{}, Host:{}", innerWal.getWalId(), trigger.getTriggerId(), this.hostLabel);
                return innerWal;
            }
            logger.info("Exclusive Trigger Failure. Trigger:{}, Host:{}", triggerId, this.hostLabel);
            return null;
        });
        if (null == wal) {
            return;
        }

        // miss
        if (missed) {
            missedTrigger(trigger, wal);
            return;
        }

        // immediate
        if (delayMS <= 0) {
            offerDelayQueue(wal, 0L);
            return;
        }

        // schedule
        offerDelayQueue(wal, delayMS);
    }


    private void missedTrigger(Trigger trigger, Wal wal) {
        MissedStrategy missedStrategy = trigger.getMissedStrategy();
        logger.info("MissedStrategy is:{}.Wal:{},Trigger:{}, Host:{}", missedStrategy, wal.getWalId(), wal.getTriggerId(), this.hostLabel);
        switch (missedStrategy) {
            case COMPENSATE:
                offerDelayQueue(wal, 0L);
                break;
            case SKIP:
                confirmWal(wal);
                logger.info("Skip missed Wal:{}, Trigger:{}", wal.getWalId(), trigger.getTriggerId());
                break;
            default:
                break;
        }
    }


    private void offerDelayQueue(Wal wal, long delayMS) {
        if (delayMS < 100) {
            logger.info("immediate tasks. delayMS:{},Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), this.hostLabel);
            triggerPool.execute(() -> confirmWalAndCreateTask(wal));
        } else {
            logger.info("asynchronous tasks. delayMS:{}, Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), this.hostLabel);
            triggerPool.schedule(() -> confirmWalAndCreateTask(wal), delayMS, TimeUnit.MILLISECONDS);
        }
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
                taskManager.create(this.hostLabel, wal);
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

}