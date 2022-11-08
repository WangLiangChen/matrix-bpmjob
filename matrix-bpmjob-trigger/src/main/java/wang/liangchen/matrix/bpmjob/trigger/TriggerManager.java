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
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.data.util.TransactionUtil;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import javax.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
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

    private final Byte triggersLoadInterval = 15;
    private final Byte triggersSchedulerThreadNumber = 20;
    private final Byte triggersJoberThreadNumber = 20;
    private final Short batchSize = 100;
    private final ScheduledExecutorService triggersLoader = Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("job-trigger-"));
    private final ScheduledExecutorService triggersScheduler = Executors.newScheduledThreadPool(triggersSchedulerThreadNumber, new CustomizableThreadFactory("job-scheduler-"));
    private final ExecutorService triggersJober = Executors.newFixedThreadPool(triggersJoberThreadNumber, new CustomizableThreadFactory("job-jober-"));

    @Inject
    public TriggerManager(StandaloneDao repository, HostManager hostManager, TaskManager taskManager) {
        this.repository = repository;
        Host host = hostManager.getHost();
        this.hostId = host.getHostId();
        this.hostLabel = host.getHostLabel();
        this.taskManager = taskManager;
        startFetchCandidateTriggers();
    }

    public void startFetchCandidateTriggers() {
        triggersLoader.scheduleWithFixedDelay(() -> {
            Set<Long> triggerIds = loadTriggers();
            triggerIds.forEach(this::exclusiveTrigger);
        }, 0, triggersLoadInterval, TimeUnit.SECONDS);
    }

    private Set<Long> loadTriggers() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        // 未来15S之前应该触发的所有触发器
        LocalDateTime range = now.plusSeconds(triggersLoadInterval);
        Criteria<Trigger> criteria = Criteria.of(Trigger.class)
                // id only
                .resultFields(Trigger::getTriggerId)
                ._lessThan(Trigger::getTriggerNext, range)
                ._equals(Trigger::getState, TriggerState.NORMAL)
                .pageSize(batchSize).pageNumber(1);
        List<Trigger> triggers = repository.list(criteria);
        Set<Long> triggerIds = triggers.stream().map(Trigger::getTriggerId).collect(Collectors.toSet());
        logger.info("accquired triggers: {}", triggerIds);
        return triggerIds;
    }

    private void exclusiveTrigger(Long triggerId) {
        Trigger trigger = repository.select(Criteria.of(Trigger.class)
                .resultFields(Trigger::getTriggerExpression, Trigger::getTriggerNext, Trigger::getMissThreshold, Trigger::getMissStrategy, Trigger::getTriggerParams, Trigger::getState)
                ._equals(Trigger::getTriggerId, triggerId));

        if (!Objects.equals(TriggerState.NORMAL.name(), trigger.getState().name())) {
            logger.info("Trigger '{}' isn't NORMAL,skipped by Host:{}", triggerId, this.hostLabel);
            return;
        }
        logger.info("Trigger '{}' is desired by Host:{}", triggerId, this.hostLabel);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerNext = trigger.getTriggerNext();
        long delay = Duration.between(now, triggerNext).getSeconds();
        Byte missThreshold = trigger.getMissThreshold();
        LocalDateTime newTriggerNext;
        if (delay <= -missThreshold) {
            // missed
            newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), now);
        } else {
            newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), triggerNext);
        }
        TransactionUtil.INSTANCE.execute(() -> {
            // 通过update抢占操作权
            boolean renew = renewTriggerNext(triggerId, triggerNext, newTriggerNext);
            if (!renew) {
                return;
            }
            // miss
            if (delay <= -missThreshold) {
                missTrigger(trigger);
                return;
            }
            // immediate
            if (delay > -missThreshold && delay <= 0) {
                immediateTrigger(trigger);
                return;
            }
            // schedule
            scheduleTrigger(trigger, delay);
        });
    }

    private void missTrigger(Trigger trigger) {
        MissStrategy missStrategy = trigger.getMissStrategy();
        logger.info("MissStrategy is:{}.Trigger:{}, Host:{}", missStrategy, trigger.getTriggerId(), this.hostLabel);
        switch (missStrategy) {
            case COMPENSATE:
                immediateTrigger(trigger);
                break;
            default:
                break;
        }
    }

    private void immediateTrigger(Trigger trigger) {
        logger.info("create wal and create task asynchronously.Trigger:{}, Host:{}", trigger.getTriggerId(), this.hostLabel);
        Long walId = createWal(trigger.getTriggerId(), trigger.getTriggerNext());
        asyncCreateTask(trigger, walId);
    }

    private void scheduleTrigger(Trigger trigger, long delay) {
        logger.info("create wal and delay:{}.,Trigger:{}, Host:{}", delay, trigger.getTriggerId(), this.hostLabel);
        Long walId = createWal(trigger.getTriggerId(), trigger.getTriggerNext());
        // add to delayed queue
        triggersScheduler.schedule(() -> asyncCreateTask(trigger, walId), delay, TimeUnit.SECONDS);
    }

    private void asyncCreateTask(Trigger trigger, Long walId) {
        triggersJober.execute(() -> {
            TransactionUtil.INSTANCE.execute(() -> {
                confirmWal(walId);
                createTask(walId, trigger);
            });
        });
    }

    private void createTask(Long walId, Trigger trigger) {
        Task task = Task.newInstance();
        task.setParentId(0L);
        task.setHostId(this.hostId);
        task.setTriggerId(trigger.getTriggerId());
        task.setWalId(walId);
        task.setTaskGroup("N/A");
        task.setTriggerParams(trigger.getTriggerParams());
        task.setTaskParams("");
        task.setParentParams("");
        taskManager.add(task);
    }

    private boolean renewTriggerNext(Long triggerId, LocalDateTime triggerNext, LocalDateTime newTriggerNext) {
        Trigger trigger = Trigger.newInstance();
        trigger.setTriggerNext(newTriggerNext);
        UpdateCriteria<Trigger> updateCriteria = UpdateCriteria.of(trigger)
                ._equals(Trigger::getTriggerId, triggerId)
                ._equals(Trigger::getTriggerNext, triggerNext)
                ._equals(Trigger::getState, TriggerState.NORMAL);
        int rows = repository.update(updateCriteria);
        if (1 == rows) {
            logger.info("Exclusive Trigger Success: {}. Host:{}", triggerId, this.hostLabel);
            return true;
        }
        logger.info("Exclusive Trigger Failure: {}. Host:{}", triggerId, this.hostLabel);
        return false;
    }

    private LocalDateTime resolveTriggerNext(String expression, LocalDateTime benchmark) {
        CronExpression cronExpression = CronExpression.parse(expression);
        return cronExpression.next(benchmark);
    }

    private Long createWal(Long triggerId, LocalDateTime scheduleDatetime) {
        Wal wal = Wal.newInstance();
        wal.setTriggerId(triggerId);
        wal.setHostId(this.hostId);
        LocalDateTime now = LocalDateTime.now();
        wal.setCreateDatetime(now);
        wal.setScheduleDatetime(scheduleDatetime);
        wal.setTriggerDatetime(now);
        wal.setState(WalState.ACQUIRED.getState());
        repository.insert(wal);
        return wal.getWalId();
    }

    private void confirmWal(Long walId) {
        Wal wal = Wal.newInstance();
        wal.setWalId(walId);
        wal.setState(WalState.TRIGGERED.getState());
        wal.setTriggerDatetime(LocalDateTime.now());
        repository.update(wal);
    }

    @Override
    public void destroy() throws Exception {
        triggersLoader.shutdown();
        triggersScheduler.shutdown();
        triggersJober.shutdown();
    }
}