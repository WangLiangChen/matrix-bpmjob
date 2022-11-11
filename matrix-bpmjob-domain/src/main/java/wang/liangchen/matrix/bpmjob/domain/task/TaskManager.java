package wang.liangchen.matrix.bpmjob.domain.task;

import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import javax.inject.Inject;
import java.util.List;


/**
 * @author Liangchen.Wang
 */
@Service
@DomainService
public class TaskManager {
    private final StandaloneDao repository;

    @Inject
    public TaskManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public int add(Task entity) {
        return repository.insert(entity);
    }

    public int delete(Long taskId) {
        Task entity = Task.newInstance();
        entity.setTaskId(taskId);
        return repository.delete(entity);
    }

    public int update(Task entity) {
        return repository.update(entity);
    }

    public Task byKey(Long taskId, String... resultColumns) {
        return repository.select(Criteria.of(Task.class)
                .resultColumns(resultColumns)
                ._equals(Task::getTaskId, taskId)
        );
    }

    public int stateTransfer(Long taskId, Byte to, Byte... from) {
        Task entity = Task.newInstance();
        entity.setState(to);
        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(entity)
                ._equals(Task::getTaskId, taskId)
                ._in(Task::getState, from);
        return repository.update(updateCriteria);
    }

    public List<Task> byStates(Short... states) {
        return repository.list(Criteria.of(Task.class)._in(Task::getState, states));
    }
}