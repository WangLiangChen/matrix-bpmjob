package wang.liangchen.matrix.bpmjob.trigger;

import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.task.Task;
import wang.liangchen.matrix.bpmjob.domain.task.TaskState;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import javax.inject.Inject;
import java.util.List;


/**
 * @author Liangchen.Wang
 */
@Service("Trigger_TaskManager")
@DomainService
public class TaskManager {
    private final StandaloneDao repository;

    @Inject
    public TaskManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public int add(Task entity) {
        entity.setState(TaskState.UNASSIGNED.getState());
        return repository.insert(entity);
    }
}