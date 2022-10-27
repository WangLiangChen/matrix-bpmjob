package wang.liangchen.matrix.bpmjob.trigger.domain.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.trigger.domain.host.Registry;
import wang.liangchen.matrix.bpmjob.trigger.domain.host.RegistryManager;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.enumeration.TriggerState;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.enumeration.WalState;
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
@Service
@DomainService
public class TriggerManager implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(TriggerManager.class);
    private final StandaloneDao repository;
    private final Short triggerInterval = 15;
    private final Short batchSize = 100;
    private final ScheduledExecutorService triggerLoader = Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("job-trigger-"));
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10, new CustomizableThreadFactory("job-scheduler-"));
    private final ExecutorService jober = Executors.newFixedThreadPool(20, new CustomizableThreadFactory("job-jober-"));

    private final Registry registry;

    @Inject
    public TriggerManager(StandaloneDao repository, RegistryManager registryManager) {
        this.repository = repository;
        this.registry = registryManager.getRegistry();
        startScheduler();

    }

    public void startScheduler() {
        triggerLoader.scheduleWithFixedDelay(() -> {
            Set<Long> triggerIds = acquireTriggers();
            triggerIds.forEach(this::exclusiveTrigger);
        }, 0, triggerInterval, TimeUnit.SECONDS);
    }

    private Set<Long> acquireTriggers() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        LocalDateTime range = now.plusSeconds(triggerInterval);
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
        logger.info("trigger '{}' is desired", triggerId);
        Trigger trigger = repository.select(Criteria.of(Trigger.class)
                .resultFields(Trigger::getTriggerExpression, Trigger::getTriggerNext, Trigger::getMissThreshold, Trigger::getMissStrategy, Trigger::getState)
                ._equals(Trigger::getTriggerId, triggerId));

        if (!Objects.equals(TriggerState.NORMAL.name(), trigger.getState())) {
            logger.info("trigger isn't NORMAL,skipped");
            return;
        }
        LocalDateTime triggerNext = trigger.getTriggerNext();
        long delay = Duration.between(LocalDateTime.now(), triggerNext).getSeconds();
        Byte missThreshold = trigger.getMissThreshold();
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
    }

    private void missTrigger(Trigger trigger) {
        // 计算下次触发时间，以当前时间为基准
        LocalDateTime newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), LocalDateTime.now());
        TransactionUtil.INSTANCE.execute(() -> {
            boolean renew = renewTriggerNext(trigger.getTriggerId(), trigger.getTriggerNext(), newTriggerNext);
            if (!renew) {
                logger.warn("Exclusive failure,Trigger: {}, Host: {}", trigger.getTriggerId(), registry.getHostName());
                return;
            }
            logger.warn("Exclusive success,Trigger: {}, Host: {}", trigger.getTriggerId(), registry.getHostName());
            switch (trigger.getMissStrategy()) {
                case COMPENSATE:
                    Long walId = createWal(trigger.getTriggerId(), trigger.getTriggerNext());
                    createTask(trigger, walId);
                    break;
                default:
                    break;
            }
        });
    }

    private void immediateTrigger(Trigger trigger) {
        // 计算下次触发时间
        LocalDateTime triggerNext = trigger.getTriggerNext();
        LocalDateTime newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), triggerNext);
        TransactionUtil.INSTANCE.execute(() -> {
            boolean renew = renewTriggerNext(trigger.getTriggerId(), triggerNext, newTriggerNext);
            if (!renew) {
                logger.warn("Exclusive failure,Trigger: {}, Host: {}", trigger.getTriggerId(), registry.getHostName());
                return;
            }
            Long walId = createWal(trigger.getTriggerId(), trigger.getTriggerNext());
            createTask(trigger, walId);
        });
    }

    private void scheduleTrigger(Trigger trigger, long delay) {
        Long triggerId = trigger.getTriggerId();
        LocalDateTime triggerNext = trigger.getTriggerNext();
        // 计算下次触发
        LocalDateTime newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), triggerNext);
        // 在同一个事务中执行
        TransactionUtil.INSTANCE.execute(() -> {
            // 通过更新抢占触发权
            boolean renew = renewTriggerNext(triggerId, triggerNext, newTriggerNext);
            // 获得触发权，写预写日志，入队等待触发
            if (renew) {
                logger.info("Exclusive success,host: {}, triggerId: {}", registry.getHostName(), triggerId);
                Long walId = createWal(triggerId, triggerNext);
                // !import add to delayed queue
                scheduler.schedule(() -> createTask(trigger, walId), delay, TimeUnit.SECONDS);
                logger.info("Schedule success,host: {}, triggerId: {}, delay: {}s", registry.getHostName(), triggerId, delay);
            } else {
                logger.info("Exclusive failure,host: {}, triggerId: {}", registry.getHostName(), triggerId);
            }
        });
    }

    private void createTask(Trigger trigger, Long walId) {
        jober.execute(() -> {
            TransactionUtil.INSTANCE.execute(() -> {
                confirmWal(walId);
                // TODO createTask
            });
        });
    }

    private boolean renewTriggerNext(Long triggerId, LocalDateTime triggerNext, LocalDateTime newTriggerNext) {
        Trigger trigger = Trigger.newInstance();
        trigger.setTriggerNext(newTriggerNext);
        UpdateCriteria<Trigger> updateCriteria = UpdateCriteria.of(trigger)
                ._equals(Trigger::getTriggerId, triggerId)
                ._equals(Trigger::getTriggerNext, triggerNext);
        return 1 == repository.update(updateCriteria);
    }

    private LocalDateTime resolveTriggerNext(String expression, LocalDateTime benchmark) {
        CronExpression cronExpression = CronExpression.parse(expression);
        return cronExpression.next(benchmark);
    }

    private Long createWal(Long triggerId, LocalDateTime scheduleDatetime) {
        Wal wal = Wal.newInstance();
        wal.setTriggerId(triggerId);
        wal.setRegistryId(registry.getRegistryId());
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
        triggerLoader.shutdown();
        scheduler.shutdown();
    }
}