package wang.liangchen.matrix.bpmjob.sdk.core.connector;

import wang.liangchen.matrix.bpmjob.api.TaskResponse;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public interface Connector {

    CompletionStage<List<TaskResponse>> getTasks(int number);

    CompletionStage<Void> acceptTasks(Set<Long> taskIds);

    CompletionStage<Void> completeTask(Long taskId);

    CompletionStage<Void> completeTask(Long taskId, Throwable throwable);
}
