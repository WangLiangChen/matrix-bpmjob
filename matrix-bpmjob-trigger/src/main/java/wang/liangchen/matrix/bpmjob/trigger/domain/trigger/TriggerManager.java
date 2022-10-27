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
    private final ScheduledExecutorService triggerLoader = Executors.newScheduledThreadPool(1, new CustomizableThreadFactory("job-trigger-"));
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10, new CustomizableThreadFactory("job-scheduler-"));

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

    /**
     * last trigger datetime < +15S
     *
     * @return accquired trigger ids
     */
    private Set<Long> acquireTriggers() {
        LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
        LocalDateTime max = now.plusSeconds(triggerInterval);
        int limit = 100;
        Criteria<Trigger> criteria = Criteria.of(Trigger.class).resultFields(Trigger::getTriggerId)
                ._lessThan(Trigger::getTriggerNext, max)
                ._equals(Trigger::getState, TriggerState.NORMAL)
                .pageSize(limit).pageNumber(1);
        List<Trigger> triggers = repository.list(criteria);
        Set<Long> triggerIds = triggers.stream().map(Trigger::getTriggerId).collect(Collectors.toSet());
        logger.info("accquired triggers: {}", triggerIds);
        return triggerIds;
    }

    private void exclusiveTrigger(Long triggerId) {
        // query by key
        Trigger trigger = repository.select(Criteria.of(Trigger.class)
                .resultFields(Trigger::getTriggerExpression, Trigger::getTriggerNext, Trigger::getMissThreshold, Trigger::getMissStrategy, Trigger::getState)
                ._equals(Trigger::getTriggerId, triggerId));

        if (!Objects.equals(TriggerState.NORMAL.name(), trigger.getState())) {
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
        // 以当前时间为基准，计算下次触发时间
        LocalDateTime newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), LocalDateTime.now());
        // 重置下发时间
        boolean refreshed = refreshTriggerNext(trigger.getTriggerId(), trigger.getTriggerNext(), newTriggerNext);
        // 被别的节点抢先了
        if (!refreshed) {
            logger.warn("Trigger '{}' is missed and skipped,host:{}", trigger.getTriggerId(), registry.getHostName());
        }
        logger.warn("Trigger '{}' is missed and refreshed,host:{}", trigger.getTriggerId(), registry.getHostName());
        switch (trigger.getMissStrategy()) {
            case COMPENSATE:
                break;
            default:
                break;
        }

    }

    private void immediateTrigger(Trigger trigger) {
        // TODO 任务线程池 同一事务域 更新预写日志 创建任务
    }

    private void scheduleTrigger(Trigger trigger, long delay) {
        Long triggerId = trigger.getTriggerId();
        LocalDateTime triggerNext = trigger.getTriggerNext();
        // 计算下次触发
        LocalDateTime newTriggerNext = resolveTriggerNext(trigger.getTriggerExpression(), triggerNext);
        // 在同一个事务中执行
        TransactionUtil.INSTANCE.execute(() -> {
            // 通过更新抢占触发权
            boolean refreshed = refreshTriggerNext(triggerId, triggerNext, newTriggerNext);
            // 获得触发权，写预写日志，入队等待触发
            if (refreshed) {
                logger.info("Exclusive success,host: {}, triggerId: {}", registry.getHostName(), triggerId);
                Wal wal = Wal.newInstance();
                wal.setTriggerId(triggerId);
                wal.setRegistryId(registry.getRegistryId());
                wal.setCreateDatetime(LocalDateTime.now());
                wal.setScheduleDatetime(triggerNext);
                wal.setTriggerDatetime(wal.getCreateDatetime());
                wal.setState(WalState.ACQUIRED.getState());
                repository.insert(wal);
                scheduler.schedule(() -> immediateTrigger(trigger), delay, TimeUnit.SECONDS);
                logger.info("Schedule success,host: {}, triggerId: {}, delay: {}s", registry.getHostName(), triggerId, delay);
            }
        });
    }

    private boolean refreshTriggerNext(Long triggerId, LocalDateTime triggerNext, LocalDateTime newTriggerNext) {
        Trigger trigger = Trigger.newInstance();
        trigger.setTriggerNext(newTriggerNext);
        UpdateCriteria<Trigger> updateCriteria = UpdateCriteria.of(trigger)._equals(Trigger::getTriggerId, triggerId)._equals(Trigger::getTriggerNext, triggerNext);
        return 1 == repository.update(updateCriteria);
    }

    private LocalDateTime resolveTriggerNext(String expression, LocalDateTime benchmark) {
        CronExpression cronExpression = CronExpression.parse(expression);
        return cronExpression.next(benchmark);
    }

    @Override
    public void destroy() throws Exception {
        triggerLoader.shutdown();
        scheduler.shutdown();
    }
}