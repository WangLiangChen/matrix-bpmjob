package wang.liangchen.matrix.bpmjob.trigger;

import jakarta.inject.Inject;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.task.Task;
import wang.liangchen.matrix.bpmjob.domain.task.TaskState;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;


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
        entity.initializeFields();
        return repository.insert(entity);
    }
}