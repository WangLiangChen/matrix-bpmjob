package wang.liangchen.matrix.bpmjob.trigger;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.bpmjob.common.thread.thread.BpmjobThread;
import wang.liangchen.matrix.bpmjob.common.thread.thread.BpmjobThreadFactory;
import wang.liangchen.matrix.bpmjob.common.utils.DateTimeUtil;
import wang.liangchen.matrix.bpmjob.common.utils.ThreadUtil;
import wang.liangchen.matrix.bpmjob.trigger.datasource.ConnectionManager;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.TriggerTime;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Wal;
import wang.liangchen.matrix.bpmjob.trigger.enumeration.MissedStrategy;
import wang.liangchen.matrix.bpmjob.trigger.enumeration.TriggerState;
import wang.liangchen.matrix.bpmjob.trigger.properties.TriggerProperties;
import wang.liangchen.matrix.bpmjob.trigger.repository.ITriggerRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @author Liangchen.Wang 2023-07-28 14:49
 */
public class BpmjobTrigger {
    private final static Logger logger = LoggerFactory.getLogger(BpmjobTrigger.class);
    private final CronParser quartzCronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
    private final ITriggerRepository triggerRepository;
    private final TriggerProperties triggerProperties;

    // default state is shutdown
    private volatile boolean halted = true;

    // TriggerThread/WalThread/
    private final CountDownLatch countDownLatch = new CountDownLatch(2);
    private final BpmjobTriggerThread triggerThread = new TriggerTriggerThread();
    private final BpmjobTriggerThread walThread = new WalTriggerThread();
    private final ScheduledThreadPoolExecutor triggerPool;

    public BpmjobTrigger(ITriggerRepository triggerRepository, TriggerProperties triggerProperties) {
        this.triggerRepository = triggerRepository;
        this.triggerProperties = triggerProperties;
        this.triggerPool = new ScheduledThreadPoolExecutor(triggerProperties.getThreadNumber(), new BpmjobThreadFactory("job-"));
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


    private class TriggerTriggerThread extends BpmjobTriggerThread {
        public TriggerTriggerThread() {
            super("trigger-");
        }

        @Override
        public long runOrSleep() {
            /*--------------------------acquire eligible triggers-------------------------------*/
            List<TriggerTime> triggerTimes = acquireTriggerTimes();
            if (null == triggerTimes || triggerTimes.isEmpty()) {
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
        List<TriggerTime> triggerTimes = this.triggerRepository.loadWaitingTriggers(this.triggerProperties.getAcquireTriggerDuration(), this.triggerProperties.getBatchSize());
        // 乱序
        Collections.shuffle(triggerTimes);
        return triggerTimes;
    }

    private void exclusiveTrigger(TriggerTime triggerTime) {
        Long triggerId = triggerTime.getTriggerId();
        Trigger trigger = this.triggerRepository.loadWaitingTrigger(triggerId);
        if (null == trigger) {
            logger.info("Trigger '{}' doesn't exist", triggerId);
            return;
        }
        String hostLabel = this.triggerProperties.getHostLabel();
        if (!Objects.equals(TriggerState.NORMAL, trigger.getState())) {
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
        ExecutionTime executionTime = ExecutionTime.forCron(quartzCronParser.parse(trigger.getTriggerCron()));
        Optional<ZonedDateTime> nextExecutionDateTime = executionTime.nextExecution(ZonedDateTime.from(benchmark));
        nextExecutionDateTime.ifPresent(zonedDateTime -> {
            // 略过本次触发,只更新下次触发时间
            if (missed && Objects.equals(MissedStrategy.SKIP.name(), trigger.getMissedStrategy())) {
                boolean success = this.triggerRepository.compareAndSwapTriggerTime(triggerId, triggerInstant, zonedDateTime.toLocalDateTime());
                if (success) {
                    logger.info("Exclusive Trigger Success. Trigger:{}, Host:{}", triggerId, hostLabel);
                    return;
                }
                logger.info("Exclusive Trigger Failure. Trigger:{}, Host:{}", triggerId, hostLabel);
                return;
            }
            // 在同一事务中,通过update抢占操作权和创建预写日志
            Optional<Wal> walOptional = this.triggerRepository.createWal(hostLabel, triggerId, triggerInstant, zonedDateTime.toLocalDateTime());
            if (walOptional.isPresent()) {
                logger.info("Wal created, Wal:{},Trigger:{}, Host:{}", wal.getWalId(), trigger.getTriggerId(), hostLabel);
                offerDelayQueue(walOptional.get(), delayMS);
                return;
            }

        });
    }

    private class WalTriggerThread extends BpmjobTriggerThread {
        WalTriggerThread() {
            super("wal-");
        }

        @Override
        public long runOrSleep() {
            List<Long> walIds = acquireEligibleWals();
            if (null == walIds || walIds.isEmpty()) {
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

    private abstract class BpmjobTriggerThread extends Thread {
        public BpmjobTriggerThread(String name) {
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
                    logger.error("An error occurred while running the instance of {}", className, e);
                }
            }
            // countdown when the 'while' is break;
            countDownLatch.countDown();
        }

        public abstract long runOrSleep();
    }
}
