package wang.liangchen.matrix.bpmjob.sdk.connector;

import wang.liangchen.matrix.bpmjob.api.HeartbeatRequest;
import wang.liangchen.matrix.bpmjob.api.TaskRequest;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.Connector;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.JavaBeanExecutorKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Liangchen.Wang 2023-06-23 23:11
 */
public class DefaultHttpConnector implements Connector {


    @Override
    public CompletionStage<List<TaskResponse>> getTasks(int number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("number", String.valueOf(number));
        return null;
    }

    @Override
    public CompletionStage<Void> acceptTasks(Set<Long> taskIds) {
        return null;
    }

    @Override
    public CompletionStage<Void> completeTask(TaskRequest taskRequest) {
        return null;
    }

    @Override
    public CompletionStage<Void> reportMethods(Set<JavaBeanExecutorKey> methods) {
        return null;
    }

    @Override
    public CompletionStage<Void> heartbeat(HeartbeatRequest heartbeatRequest) {
        return null;
    }
}
