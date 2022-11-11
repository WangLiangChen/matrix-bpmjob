package wang.liangchen.matrix.bpmjob.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.host.Host;
import wang.liangchen.matrix.bpmjob.domain.task.Task;
import wang.liangchen.matrix.bpmjob.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.domain.trigger.Wal;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.TriggerState;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.WalState;
import wang.liangchen.matrix.framework.commons.collection.CollectionUtil;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.thread.ThreadUtil;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.data.util.TransactionUtil;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author Liangchen.Wang
 */
@Service("Trigger_TriggerManager")
@DomainService
public class TriggerManager implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(TriggerManager.class);
    private final StandaloneDao repository;
    private final Long hostId;
    private final String hostLabel;
    private final TaskManager taskManager;

    private final Byte triggersAcquireInterval = 15;
    private final Short batchSize = 100;
    private final TriggerThread triggerThread = new TriggerThread();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(32, new CustomizableThreadFactory("job-scheduler-"));

    @Inject
    public TriggerManager(StandaloneDao repository, HostManager hostManager, TaskManager taskManager) {
        this.repository = repository;
        Host host = hostManager.getHost();
        this.hostId = host.getHostId();
        this.hostLabel = host.getHostLabel();
        this.taskManager = taskManager;
        this.triggerThread.start();
    }

    @Override
    public void destroy() throws Exception {
        // halt TriggerThread
        this.triggerThread.halt();
        logger.info("Waiting for scheduler thread pool shutdown ...");
        scheduler.awaitTermination(1, TimeUnit.MINUTES);
    }

    /**
     * Trigger main process
     */
    private class TriggerThread extends Thread {
        private boolean halted = false;

        public TriggerThread() {
            super("job-scheduler");
        }

        void halt() {
            this.halted = true;
        }

        @Override
        public void run() {
            /*--------------------------acquire triggers and fire-------------------------------*/
            long acquireRetryCount = 0;
            while (true) {
                List<Long> triggerIds;
                try {
                    if (acquireRetryCount > 0) {
                        ThreadUtil.INSTANCE.sleep(TimeUnit.SECONDS, 15);
                    }
                    triggerIds = acquireTriggers();
                    logger.info("Triggers acquired: {}", triggerIds);
                    acquireRetryCount = 0;
                    if (CollectionUtil.INSTANCE.isEmpty(triggerIds)) {
                        logger.info("Triggers acquired are empty.delay and then retry");
                        ThreadUtil.INSTANCE.sleep(TimeUnit.SECONDS, 1);
                    }
                } catch (Exception e) {
                    acquireRetryCount++;
                    logger.error("AcquireTriggers failed.Retry: " + acquireRetryCount, e);
                    continue;
                }
                // 依次在独立事务中抢占触发权后触发
                for (Long triggerId : triggerIds) {
                    try {
                        exclusiveTrigger(triggerId);
                    } catch (Exception e) {
                        logger.error("ExclusiveTrigger failed.Trigger: " + triggerId, e);
                    }
                }
                /*--------------------------acquire wals and redo-------------------------------*/
                List<Long> walIds = null;
                try {
                    walIds = acquireWals();
                    logger.info("Triggers acquired: {}", triggerIds);
                } catch (Exception e) {
                    logger.error("AcquireWals failed.", e);
                }
                if (null != walIds && !walIds.isEmpty()) {
                    for (Long walId : walIds) {
                        try {
                            redoTask(walId);
                        } catch (Exception e) {
                            logger.error("redoTask failed.Wal: " + walId, e);
                        }
                    }
                }
                /*--------------------------shutdown-------------------------------*/
                // shutdown threadpool and break when execution complete.
                if (halted) {
                    logger.info("TriggerThread is halted. Shutdown...");
                    scheduler.shutdown();
                    break;
                }
            }
        }
    }

    private List<Long> acquireTriggers() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        // 未来15S之前应该触发的所有触发器
        LocalDateTime range = now.plusSeconds(triggersAcquireInterval);
        Criteria<Trigger> criteria = Criteria.of(Trigger.class)
                // id only
                .resultFields(Trigger::getTriggerId)
                ._lessThan(Trigger::getTriggerNext, range)
                ._equals(Trigger::getState, TriggerState.NORMAL)
                .pageSize(batchSize).pageNumber(1);
        List<Trigger> triggers = repository.list(criteria);
        List<Long> triggerIds = triggers.stream().map(Trigger::getTriggerId).collect(Collectors.toList());
        // 乱序
        Collections.shuffle(triggerIds);
        return triggerIds;
    }

    private List<Long> acquireWals() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        // 当前时间之前1S的Wal
        LocalDateTime range = now.minusSeconds(1);
        Criteria<Wal> criteria = Criteria.of(Wal.class)
                // id only
                .resultFields(Wal::getWalId)
                ._lessThan(Wal::getScheduleDatetime, range)
                .pageSize(batchSize).pageNumber(1);
        List<Wal> wals = repository.list(criteria);
        List<Long> walIds = wals.stream().map(Wal::getWalId).collect(Collectors.toList());
        // 乱序
        Collections.shuffle(walIds);
        return walIds;
    }

    private void exclusiveTrigger(Long triggerId) {
        Trigger trigger = repository.select(Criteria.of(Trigger.class)
                .resultFields(Trigger::getTriggerId, Trigger::getTriggerExpression, Trigger::getTriggerNext, Trigger::getMissThreshold, Trigger::getMissStrategy, Trigger::getTriggerParams, Trigger::getState)
                ._equals(Trigger::getTriggerId, triggerId));
        if (null == trigger) {
            logger.info("Trigger '{}' doesn't exist,skipped by Host:{}", triggerId, hostLabel);
            return;
        }

        if (!Objects.equals(TriggerState.NORMAL.name(), trigger.getState().name())) {
            logger.info("Trigger '{}' isn't NORMAL,skipped by Host:{}", triggerId, hostLabel);
            return;
        }

        logger.info("Trigger '{}' is desired by Host:{}", triggerId, hostLabel);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerNext = trigger.getTriggerNext();
        long delayMS = Duration.between(now, triggerNext).toMillis();
        long missThresholdMS = trigger.getMissThreshold() * 1000;
        LocalDateTime newTriggerNext;
        if (delayMS <= -missThresholdMS) {
            // missed,benchmark is now
            newTriggerNext = CronExpression.parse(trigger.getTriggerExpression()).next(now);
        } else {
            newTriggerNext = CronExpression.parse(trigger.getTriggerExpression()).next(triggerNext);
        }
        // 在同一事务中,通过update抢占操作权和创建预写日志
        Wal wal = TransactionUtil.INSTANCE.execute(() -> {
            boolean renew = renewTriggerNext(triggerId, triggerNext, newTriggerNext);
            if (!renew) {
                logger.info("Exclusive Trigger Failure. Trigger:{}, Host:{}", triggerId, this.hostLabel);
                return null;
            }
            logger.info("Exclusive Trigger Success. Trigger:{}, Host:{}", triggerId, this.hostLabel);
            Wal innerWal = createWal(trigger);
            logger.info("Wal created, Wal:{},Trigger:{}, Host:{}", innerWal.getWalId(), trigger.getTriggerId(), this.hostLabel);
            return innerWal;
        });
        if (null == wal) {
            return;
        }

        // miss
        if (delayMS <= -missThresholdMS) {
            missTrigger(wal, trigger);
            return;
        }

        // immediate
        if (delayMS > -missThresholdMS && delayMS <= 0) {
            scheduleTrigger(wal, 0L);
            return;
        }

        // schedule
        scheduleTrigger(wal, delayMS);
    }

    private boolean renewTriggerNext(Long triggerId, LocalDateTime triggerNext, LocalDateTime newTriggerNext) {
        Trigger trigger = Trigger.newInstance();
        trigger.setTriggerNext(newTriggerNext);
        UpdateCriteria<Trigger> updateCriteria = UpdateCriteria.of(trigger)
                ._equals(Trigger::getTriggerId, triggerId)
                ._equals(Trigger::getTriggerNext, triggerNext)
                ._equals(Trigger::getState, TriggerState.NORMAL);
        int rows = repository.update(updateCriteria);
        return 1 == rows;
    }


    private void redoTask(Long walId) {
        // TODO 补充wal返回的字段
        Wal wal = repository.select(Criteria.of(Wal.class)
                .resultFields(Wal::getWalId)
                ._equals(Wal::getWalId, walId));
        if (null == wal) {
            logger.info("Wal '{}' doesn't exist,Maybe It has been confirmed.skipped by Host:{}", walId, hostLabel);
            return;
        }
        logger.info("Wal '{}' is desired by Host:{}", walId, hostLabel);
        scheduleTrigger(wal, 0L);
    }

    private void missTrigger(Wal wal, Trigger trigger) {
        MissStrategy missStrategy = trigger.getMissStrategy();
        logger.info("MissStrategy is:{}.Wal:{},Trigger:{}, Host:{}", missStrategy, wal.getWalId(), wal.getTriggerId(), this.hostLabel);
        switch (missStrategy) {
            case COMPENSATE:
                scheduleTrigger(wal, 0L);
                break;
            default:
                break;
        }
    }

    private void scheduleTrigger(Wal wal, long delayMS) {
        // TODO 根据wal判断是否是redo
        if (delayMS < 100) {
            logger.info("create immediate tasks. delayMS:{},Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), this.hostLabel);
            scheduler.execute(() -> confirmWalAndCreateTask(wal));
        } else {
            logger.info("create asynchronous tasks. delayMS:{}, Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), this.hostLabel);
            scheduler.schedule(() -> confirmWalAndCreateTask(wal), delayMS, TimeUnit.MILLISECONDS);
        }
    }

    private Wal createWal(Trigger trigger) {
        Wal wal = Wal.newInstance();
        wal.setTriggerId(trigger.getTriggerId());
        wal.setHostId(this.hostId);
        wal.setWalGroup(trigger.getTriggerGroup());
        LocalDateTime now = LocalDateTime.now();
        wal.setCreateDatetime(now);
        wal.setScheduleDatetime(trigger.getTriggerNext());
        wal.setTriggerDatetime(now);
        wal.setState(WalState.ACQUIRED.getState());
        repository.insert(wal);
        return wal;
    }

    private void confirmWalAndCreateTask(Wal wal) {
        Long walId = wal.getWalId();
        // 同一事务确认(删除)Wal和创建任务
        TransactionUtil.INSTANCE.execute(() -> {
            int rows = confirmWal(wal);
            // redo 时,其它线程已经创建任务
            if (0 == rows) {
                logger.info("ConfirmWal failed.Maybe It has been confirmed.Wal: {}", walId);
                return;
            }
            createTask(wal);
            logger.info("Task created.Task: {}", walId);
        });
    }

    private int confirmWal(Wal wal) {
        return repository.delete(wal);
    }

    private void createTask(Wal wal) {
        Task task = Task.newInstance();
        task.setTaskId(wal.getWalId());
        task.setParentId(0L);
        task.setHostId(this.hostId);
        task.setTriggerId(wal.getTriggerId());
        task.setTaskGroup("N/A");
        task.setTriggerParams("");
        task.setTaskParams("");
        task.setParentParams("");
        taskManager.add(task);
    }
}