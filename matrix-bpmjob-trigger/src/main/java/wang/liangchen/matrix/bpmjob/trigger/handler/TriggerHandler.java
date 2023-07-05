package wang.liangchen.matrix.bpmjob.trigger.handler;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TriggerHandler {
    private final static Logger logger = LoggerFactory.getLogger(TriggerHandler.class);
    private final TriggerManager triggerManager;
    private final TaskManager taskManager;
    private final TriggerProperties triggerProperties;


    // default state is shutdown
    private volatile boolean halted = true;
    // TriggerThread/WalThread/TaskThread
    private final CountDownLatch countDownLatch = new CountDownLatch(3);
    private final BpmjobThread triggerThread = new TriggerThread();
    private final BpmjobThread walThread = new WalThread();
    private final BpmjobThread taskThread = new TaskThread();
    private final ScheduledThreadPoolExecutor triggerPool;


    @Inject
    public TriggerHandler(TriggerManager triggerManager, TaskManager taskManager, ObjectProvider<TriggerProperties> triggerPropertiesObjectProvider) {
        this.triggerManager = triggerManager;
        this.taskManager = taskManager;
        this.triggerProperties = triggerPropertiesObjectProvider.getIfAvailable(TriggerProperties::new);
        this.triggerPool = new ScheduledThreadPoolExecutor(triggerProperties.getThreadNumber(), new CustomizableThreadFactory("job-"));
    }

    public synchronized void start() {
        if (this.halted) {
            // start all threads
            this.walThread.start();
            this.triggerThread.start();
            this.triggerThread.start();
            // register shutdown hooker
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            this.halted = false;
        }
    }

    public synchronized void shutdown() {
        if (this.halted) {
            logger.info("the client has been shutdown");
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


    private class TriggerThread extends BpmjobThread {
        public TriggerThread() {
            super("trigger-");
        }

        @Override
        public long runOrSleep() {
            /*--------------------------acquire eligible triggers-------------------------------*/
            List<TriggerTime> triggerTimes = acquireTriggerTimes();
            if (CollectionUtil.INSTANCE.isEmpty(triggerTimes)) {
                logger.info("Triggers acquisition empty. delay '1s' and then retry");
                return 1;
            }
            /*--------------------------exclusive trigger and fire it-------------------------------*/
            for (TriggerTime triggerTime : triggerTimes) {
                try {
                    exclusiveTrigger(triggerTime);
                } catch (Exception e) {
                    logger.error("Trigger exclusive failure.Trigger: " + triggerTime.getTriggerId(), e);
                }
            }
            return 0;
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
        Trigger trigger = triggerManager.selectTrigger(triggerId, Trigger::getTriggerId, Trigger::getTriggerCron, Trigger::getMissedStrategy, Trigger::getMissedThreshold, Trigger::getState);
        if (null == trigger) {
            logger.info("Trigger '{}' doesn't exist", triggerId);
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
        TransactionUtil.INSTANCE.execute(() -> {
            boolean renew = triggerManager.renewTriggerInstant(triggerId, triggerInstant, nextTriggerInstant);
            if (!renew) {
                logger.info("Exclusive Trigger Failure. Trigger:{}, Host:{}", triggerId, hostLabel);
                return;
            }
            logger.info("Exclusive Trigger Success. Trigger:{}, Host:{}", triggerId, hostLabel);
            if (!missed || MissedStrategy.COMPENSATE == trigger.getMissedStrategy()) {
                Wal wal = triggerManager.createWal(hostLabel, triggerId, triggerInstant, JsonField.newInstance());
                logger.info("Wal created, Wal:{},Trigger:{}, Host:{}", wal.getWalId(), trigger.getTriggerId(), hostLabel);
                offerDelayQueue(wal, delayMS);
            }
        });
    }

    private class WalThread extends BpmjobThread {
        WalThread() {
            super("wal-");
        }

        @Override
        public long runOrSleep() {
            List<Long> walIds = acquireEligibleWals();
            if (CollectionUtil.INSTANCE.isEmpty(walIds)) {
                logger.info("Wals acquisition empty.delay '1s' and then retry");
                return 1;
            }
            /*--------------------------redo wals-------------------------------*/
            for (Long walId : walIds) {
                try {
                    exclusiveAndRedoWal(walId);
                } catch (Exception e) {
                    logger.error("redoWal failed.Wal: " + walId, e);
                }
            }
            return 0;
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
            logger.info("instant job. delayMS:{},Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), hostLabel);
            triggerPool.execute(() -> confirmWalAndCreateTask(wal));
            return;
        }

        logger.info("Scheduled job. delayMS:{}, Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), hostLabel);
        triggerPool.schedule(() -> confirmWalAndCreateTask(wal), delayMS, TimeUnit.MILLISECONDS);
    }

    private void confirmWalAndCreateTask(Wal wal) {
        Long walId = wal.getWalId();
        // 同一事务确认(删除)Wal和创建任务
        TransactionUtil.INSTANCE.execute(() -> {
            int rows = confirmWal(wal);
            // redo 时,其它线程已经确认这个wal并且创建任务
            if (0 == rows) {
                logger.info("Confirm Wal failed.Maybe It has been confirmed by other host.Wal: {}", walId);
                return;
            }
            Long triggerId = wal.getTriggerId();
            Trigger trigger = triggerManager.selectTrigger(triggerId, Trigger::getTriggerId, Trigger::getState, Trigger::getShardingNumber, Trigger::getTenantCode, Trigger::getAppCode, Trigger::getExecutorType, Trigger::getExecutorSettings, Trigger::getTriggerParams, Trigger::getRunningDurationThreshold);
            String hostLabel = this.triggerProperties.getHostLabel();
            if (null == trigger) {
                logger.info("Trigger '{}' doesn't exist", triggerId);
                return;
            }

            if (!Objects.equals(TriggerState.NORMAL.key(), trigger.getState().key())) {
                logger.info("The state of trigger '{}' isn't NORMAL,skipped by Host:{}", triggerId, hostLabel);
                return;
            }
            taskManager.create(hostLabel, wal, trigger);
            logger.info("Task created.Task: {}", walId);
        });
    }

    private int confirmWal(Wal wal) {
        return triggerManager.confirmWal(wal.getWalId());
    }

    private class TaskThread extends BpmjobThread {
        TaskThread() {
            super("task-");
        }

        @Override
        public long runOrSleep() {
            return 5;
        }
    }

    private abstract class BpmjobThread extends Thread {
        public BpmjobThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            String className = this.getClass().getName();
            long sleep = 0;
            while (true) {
                /*--------------------------halted and break-------------------------------*/
                if (halted) {
                    logger.info("The instance of '{}' is halted.", className);
                    break;
                }
                /*--------------------------need sleep become some reasons-------------------------------*/
                if (sleep > 0) {
                    logger.info("The instance of '{}' need to sleep {}s", className, sleep);
                    ThreadUtil.INSTANCE.sleep(TimeUnit.SECONDS, sleep);
                }
                try {
                    sleep = runOrSleep();
                } catch (Exception e) {
                    sleep = 5;
                    logger.error("An error occurred while running the instance of ".concat(className), e);
                }
            }
            // countdown when the 'while' is break;
            countDownLatch.countDown();
        }

        public abstract long runOrSleep();
    }

}