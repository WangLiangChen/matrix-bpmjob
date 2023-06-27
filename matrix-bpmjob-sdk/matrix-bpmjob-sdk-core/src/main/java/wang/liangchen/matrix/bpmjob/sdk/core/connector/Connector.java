package wang.liangchen.matrix.bpmjob.sdk.core.connector;

import wang.liangchen.matrix.bpmjob.api.HeartbeatRequest;
import wang.liangchen.matrix.bpmjob.api.TaskRequest;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.JavaBeanExecutorKey;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public interface Connector {

    CompletionStage<List<TaskResponse>> getTasks(int number);

    CompletionStage<Void> acceptTasks(Set<Long> taskIds);

    CompletionStage<Void> completeTask(TaskRequest taskRequest);

    CompletionStage<Void> reportMethods(Set<JavaBeanExecutorKey> methods);

    CompletionStage<Void> heartbeat(HeartbeatRequest heartbeatRequest);
}
