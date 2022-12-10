package wang.liangchen.matrix.bpmjob.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.host.Host;
import wang.liangchen.matrix.bpmjob.domain.host.HostState;
import wang.liangchen.matrix.bpmjob.domain.host.HostType;
import wang.liangchen.matrix.bpmjob.domain.task.Task;
import wang.liangchen.matrix.bpmjob.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.domain.trigger.Wal;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.TriggerState;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.WalState;
import wang.liangchen.matrix.framework.commons.collection.CollectionUtil;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.network.NetUtil;
import wang.liangchen.matrix.framework.commons.thread.ThreadUtil;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.data.util.TransactionUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Liangchen.Wang 2022-12-10 18:53
 */
@Service("Trigger_TriggerAndMonitorManager")
public class TriggerAndMonitorManager implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(TriggerAndMonitorManager.class);
    private final StandaloneDao repository;
    private final TaskManager taskManager;

    // Host
    private final Host host = Host.newInstance();
    private final Long hostId;
    private final String hostLabel;
    private final Host heartbeatHost = Host.newInstance();
    private final Host monitorHost = Host.newInstance();
    private final Short heartbeatInterval = 1;
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("bpmjob-beat-"));
    // Trigger
    private final Byte triggersAcquireWindow = 15;
    private final Short batchSize = 100;
    private final TriggerThread triggerThread = new TriggerThread();
    private final ScheduledExecutorService jobScheduler = Executors.newScheduledThreadPool(32, new CustomizableThreadFactory("bpmjob-"));


    public TriggerAndMonitorManager(StandaloneDao repository, TaskManager taskManager) {
        this.repository = repository;
        this.taskManager = taskManager;
        registerHost();
        this.hostId = this.host.getHostId();
        this.hostLabel = this.host.getHostLabel();

        // 设置心跳默认数据
        heartbeatHost.setHostId(this.host.getHostId());
        heartbeatHost.setState(HostState.ONLINE.getState());
        // 设置僵死状态默认数据
        monitorHost.setTerminator(this.host.getHostId());
        monitorHost.setState(HostState.DEAD.getState());
        // 启动心跳
        startHeartbeatAndMonitor();
        // 启动触发
        this.triggerThread.start();
    }

    @Override
    public void destroy() throws Exception {
        // halt TriggerThread and shutdown triggerScheduler
        this.triggerThread.halt();
        logger.info("Waiting for trigger scheduler thread pool shutdown ...");
        jobScheduler.awaitTermination(1, TimeUnit.MINUTES);

        // 停止心跳和监视
        heartbeatScheduler.shutdown();
        heartbeatScheduler.awaitTermination(1, TimeUnit.MINUTES);
        Host entity = Host.newInstance();
        entity.setHostId(this.host.getHostId());
        entity.setState(HostState.OFFLINE.getState());
        entity.setOfflineDatetime(LocalDateTime.now());
        repository.update(entity);
    }

    /**
     * register current host
     * registered as a new host when startup
     */
    private void registerHost() {
        this.host.setHostType(HostType.TRIGGER.name());
        this.host.setHostLabel(NetUtil.INSTANCE.getLocalHostName());
        this.host.setHostIp(NetUtil.INSTANCE.getLocalHostAddress());
        this.host.setHostPort((short) 0);
        this.host.setHeartbeatInterval(heartbeatInterval);
        this.host.setState(HostState.ONLINE.getState());
        this.host.initializeFields();
        repository.insert(host);
        logger.info("register host:{},{}", host.getHostId(), host.getHostLabel());
    }

    /**
     * keepalive by heartbeat
     */
    private void startHeartbeatAndMonitor() {
        heartbeatScheduler.scheduleWithFixedDelay(() -> {
            heartbeat();
            monitor();
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    private void heartbeat() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        heartbeatHost.setHeartbeatDatetime(now);
        heartbeatHost.setHeartbeatInterval(heartbeatInterval);
        repository.update(heartbeatHost);
        logger.debug("heartbeat:{},{}", host.getHostId(), host.getHostLabel());
    }

    private void monitor() {
        // 终止状态为"online"且两个周期没有心跳的其它节点
        // 1、获取所有状态为online的数据，java判断周期差
        // 2、数据库判断状态为online和周期差
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        List<Host> hosts = repository.list(Criteria.of(Host.class)
                .resultFields(Host::getHostId)
                ._equals(Host::getState, HostState.ONLINE.getState())
                ._lessThan(Host::getHeartbeatDatetime, now.minusSeconds(3L * heartbeatInterval)));
        Set<Long> hostIds = hosts.stream().map(Host::getHostId)
                .filter(e -> !e.equals(this.host.getHostId())).collect(Collectors.toSet());
        logger.debug("monitor hosts:{},{},{}", host.getHostId(), host.getHostLabel(), hostIds);
        terminateHosts(hostIds);
    }

    /**
     * Terminate Hosts
     * State transfer from online to dead
     *
     * @param hostIds Terminating hosts
     */
    private void terminateHosts(Set<Long> hostIds) {
        // 终结超时没有心跳的节点，使用状态迁移：online->dead 抢占
        for (Long hostId : hostIds) {
            monitorHost.setDeadDatetime(LocalDateTime.now());
            int rows = repository.update(UpdateCriteria.of(monitorHost)
                    ._equals(Host::getHostId, hostId)
                    ._equals(Host::getState, HostState.ONLINE.getState())
            );
            if (rows == 1) {
                logger.info("terminate host:{},{},{}", host.getHostId(), host.getHostLabel(), hostId);
                //TODO 终结主机后，要承担它原有的任务
            }
        }
    }

    /**
     * Trigger main process
     */
    private class TriggerThread extends Thread {
        private boolean halted = false;

        public TriggerThread() {
            super("bpmjob-trigger-");
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
                /*--------------------------依次在独立事务中抢占触发权后触发-------------------------------*/
                for (Long triggerId : triggerIds) {
                    try {
                        // 获得触发权&创建WAL
                        exclusiveTrigger(triggerId);
                        ThreadUtil.INSTANCE.sleep(TimeUnit.MILLISECONDS, 50);
                    } catch (Exception e) {
                        logger.error("ExclusiveTrigger failed.Trigger: " + triggerId, e);
                    }
                }
                /*--------------------------acquire wals and redo-------------------------------*/
                List<Long> walIds = null;
                try {
                    walIds = acquireWals();
                    logger.info("wals acquired: {}", walIds);
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
                    jobScheduler.shutdown();
                    break;
                }
            }
        }

        private List<Long> acquireTriggers() {
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            // 未来15S之前应该触发的所有触发器
            LocalDateTime range = now.plusSeconds(triggersAcquireWindow);
            Criteria<Trigger> criteria = Criteria.of(Trigger.class)
                    // id only
                    .resultFields(Trigger::getTriggerId)
                    ._lessThan(Trigger::getTriggerNext, range)
                    ._equals(Trigger::getState, TriggerState.NORMAL)
                    .pageSize(batchSize).pageNumber(1);
            List<Trigger> triggers = repository.list(criteria);
            List<Long> triggerIds = triggers.stream().map(Trigger::getTriggerId).collect(Collectors.toList());
            // 乱序一下
            Collections.shuffle(triggerIds);
            return triggerIds;
        }

        private void exclusiveTrigger(Long triggerId) {
            Trigger trigger = repository.select(Criteria.of(Trigger.class)
                    .resultFields(Trigger::getTriggerId, Trigger::getTriggerExpression, Trigger::getTriggerNext,
                            Trigger::getMissThreshold, Trigger::getMissStrategy, Trigger::getTriggerParams, Trigger::getState)
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
                    logger.info("Exclusive Trigger Failure. Trigger:{}, Host:{}", triggerId, hostLabel);
                    return null;
                }
                logger.info("Exclusive Trigger Success. Trigger:{}, Host:{}", triggerId, hostLabel);
                Wal innerWal = createWal(trigger);
                logger.info("Wal created, Wal:{},Trigger:{}, Host:{}", innerWal.getWalId(), trigger.getTriggerId(), hostLabel);
                return innerWal;
            });
            if (null == wal) {
                return;
            }

            /*----------------------------- 以下为异步 ----------------------------------*/
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

        private List<Long> acquireWals() {
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            // 当前时间之前1S应该已经创建任务的Wal getScheduleDatetime
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

        private void redoTask(Long walId) {
            // TODO 补充需要wal返回的字段
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
            logger.info("MissStrategy is:{}.Wal:{},Trigger:{}, Host:{}", missStrategy, wal.getWalId(), wal.getTriggerId(), hostLabel);
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
                logger.info("create immediate tasks. delayMS:{},Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), hostLabel);
                jobScheduler.execute(() -> confirmWalAndCreateTask(wal));
            } else {
                logger.info("create asynchronous tasks. delayMS:{}, Wal:{},Trigger:{}, Host:{}", delayMS, wal.getWalId(), wal.getTriggerId(), hostLabel);
                jobScheduler.schedule(() -> confirmWalAndCreateTask(wal), delayMS, TimeUnit.MILLISECONDS);
            }
        }

        private Wal createWal(Trigger trigger) {
            Wal wal = Wal.newInstance();
            wal.setTriggerId(trigger.getTriggerId());
            wal.setHostId(hostId);
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
            task.setHostId(hostId);
            task.setTriggerId(wal.getTriggerId());
            task.setTaskGroup("N/A");
            task.setTriggerParams("");
            task.setTaskParams("");
            task.setParentParams("");
            taskManager.add(task);
        }
    }
}
