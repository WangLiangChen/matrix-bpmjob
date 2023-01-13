package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.inject.Inject;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.WalState;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.DeleteCriteria;
import wang.liangchen.matrix.framework.data.dao.criteria.EntityGetter;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Liangchen.Wang
 */
@Service
public class TriggerManager {
    private final StandaloneDao repository;

    @Inject
    public TriggerManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public Trigger selectTrigger(Long triggerId, EntityGetter<Trigger>... resultFields) {
        return repository.select(Criteria.of(Trigger.class)
                .resultFields(resultFields)
                ._equals(Trigger::getTriggerId, triggerId));
    }

    public List<TriggerTime> eligibleTriggerTimes(LocalDateTime duration, int batchSize) {
        Criteria<TriggerTime> criteria = Criteria.of(TriggerTime.class)
                ._lessThan(TriggerTime::getTriggerInstant, duration)
                .pageSize(batchSize).pageNumber(1);
        return this.repository.list(criteria);
    }

    public boolean renewTriggerInstant(Long triggerId, LocalDateTime triggerInstant, LocalDateTime nextTriggerInstant) {
        TriggerTime triggerTime = TriggerTime.newInstance();
        triggerTime.setTriggerInstant(nextTriggerInstant);

        UpdateCriteria<TriggerTime> updateCriteria = UpdateCriteria.of(triggerTime)
                ._equals(TriggerTime::getTriggerId, triggerId)
                ._equals(TriggerTime::getTriggerInstant, triggerInstant);
        int rows = repository.update(updateCriteria);
        return 1 == rows;
    }

    public List<Long> eligibleWalIds(LocalDateTime duration, int batchSize) {
        Criteria<Wal> criteria = Criteria.of(Wal.class)
                .resultFields(Wal::getWalId)
                ._lessThan(Wal::getScheduleDatetime, duration)
                .pageSize(batchSize).pageNumber(1);
        List<Wal> wals = this.repository.list(criteria);
        return wals.stream().map(Wal::getWalId).collect(Collectors.toList());
    }

    public Wal selectWal(Long walId, EntityGetter<Wal>... resultFields) {
        return repository.select(Criteria.of(Wal.class)
                .resultFields(resultFields)
                ._equals(Wal::getWalId, walId));
    }

    public void createWal(Wal wal) {
        LocalDateTime now = LocalDateTime.now();
        wal.setCreateDatetime(now);
        wal.setScheduleDatetime(now);
        wal.setState(WalState.ACQUIRED.getState());
        this.repository.insert(wal);
    }

    public int deleteWal(Long walId) {
        return this.repository.delete(DeleteCriteria.of(Wal.class)._equals(Wal::getWalId, walId));
    }
}