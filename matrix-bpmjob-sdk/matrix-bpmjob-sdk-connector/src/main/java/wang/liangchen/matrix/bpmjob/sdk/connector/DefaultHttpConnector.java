package wang.liangchen.matrix.bpmjob.sdk.connector;

import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.Connector;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Liangchen.Wang 2023-06-23 23:11
 */
public class DefaultHttpConnector implements Connector {

    @Override
    public CompletionStage<List<TaskResponse>> getTasks(int number) {
        return null;
    }

    @Override
    public CompletionStage<Void> acceptTasks(Set<Long> taskIds) {
        return null;
    }

    @Override
    public CompletionStage<Void> completeTask(Long taskId) {
        return null;
    }

    @Override
    public CompletionStage<Void> completeTask(Long taskId, Throwable throwable) {
        return null;
    }
}
