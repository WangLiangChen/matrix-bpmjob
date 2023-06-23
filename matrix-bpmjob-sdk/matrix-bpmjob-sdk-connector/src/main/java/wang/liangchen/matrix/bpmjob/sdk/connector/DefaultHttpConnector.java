package wang.liangchen.matrix.bpmjob.sdk.connector;

import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.Connector;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Liangchen.Wang 2023-06-23 23:11
 */
public class DefaultHttpConnector implements Connector {
    @Override
    public void getTasks(int number, Consumer<List<TaskResponse>> resultHandler, Consumer<Throwable> throwableHandler) {
    }

    @Override
    public void acceptTasks(Set<Long> taskIds, Runnable resultHandler, Consumer<Throwable> throwableHandler) {

    }

    @Override
    public void completeTask(Long taskId, Consumer<Throwable> throwableHandler) {

    }

    @Override
    public void completeTask(Long taskId, Throwable throwable, Consumer<Throwable> throwableHandler) {

    }
}
