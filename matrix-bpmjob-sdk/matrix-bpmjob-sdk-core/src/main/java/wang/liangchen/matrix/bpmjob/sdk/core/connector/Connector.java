package wang.liangchen.matrix.bpmjob.sdk.core.connector;

import wang.liangchen.matrix.bpmjob.api.TaskResponse;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public interface Connector {

    void getTasks(int number, Consumer<List<TaskResponse>> resultHandler, Consumer<Throwable> throwableHandler);

    void acceptTasks(Set<Long> taskIds,Runnable resultHandler, Consumer<Throwable> throwableHandler);

    void completeTask(Long taskId, Consumer<Throwable> throwableHandler);
    void completeTask(Long taskId, Throwable throwable, Consumer<Throwable> throwableHandler);
}
