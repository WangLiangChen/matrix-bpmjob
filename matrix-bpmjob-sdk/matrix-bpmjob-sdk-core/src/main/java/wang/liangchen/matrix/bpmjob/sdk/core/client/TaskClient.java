package wang.liangchen.matrix.bpmjob.sdk.core.client;

import wang.liangchen.matrix.bpmjob.api.TaskResponse;

import java.util.List;
import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public interface TaskClient {

    List<TaskResponse> getTasks(int number);

    void acceptTasks(Set<Long> taskIds);

    void completeTask(Long taskId, Exception exception);
}
