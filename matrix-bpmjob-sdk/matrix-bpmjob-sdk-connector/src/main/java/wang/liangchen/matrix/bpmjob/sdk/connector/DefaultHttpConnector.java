package wang.liangchen.matrix.bpmjob.sdk.connector;

import wang.liangchen.matrix.bpmjob.api.HeartbeatRequest;
import wang.liangchen.matrix.bpmjob.api.TaskRequest;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.bpmjob.sdk.connector.utils.VertxWebClientUtil;
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
    private final DefaultHttpConnectorProperties properties;

    public DefaultHttpConnector(DefaultHttpConnectorProperties properties) {
        this.properties = properties;
    }


    @Override
    public CompletionStage<List<TaskResponse>> getTasks(int number) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("number", String.valueOf(number));
        return VertxWebClientUtil.INSTANCE.getList(properties.getTaskUri().concat("/task/getTasks"), queryParams,
                properties.getHeaders(), properties.getCredentials(), TaskResponse.class);
    }

    @Override
    public CompletionStage<Void> acceptTasks(Set<Long> taskIds) {
        return VertxWebClientUtil.INSTANCE.postJson(properties.getTaskUri().concat("/task/acceptTasks"), taskIds, properties.getHeaders(), properties.getCredentials(), Void.class);
    }

    @Override
    public CompletionStage<Void> completeTask(TaskRequest taskRequest) {
        return VertxWebClientUtil.INSTANCE.postJson(properties.getTaskUri().concat("/task/completeTask"), taskRequest, properties.getHeaders(), properties.getCredentials(), Void.class);
    }

    @Override
    public CompletionStage<Void> reportMethods(Set<JavaBeanExecutorKey> methods) {
        return VertxWebClientUtil.INSTANCE.postJson(properties.getUri().concat("/report/reportMethods"), methods, properties.getHeaders(), properties.getCredentials(), Void.class);
    }

    @Override
    public CompletionStage<Void> heartbeat(HeartbeatRequest heartbeatRequest) {
        return VertxWebClientUtil.INSTANCE.postJson(properties.getUri().concat("/report/heartbeat"), heartbeatRequest, properties.getHeaders(), properties.getCredentials(), Void.class);
    }
}
