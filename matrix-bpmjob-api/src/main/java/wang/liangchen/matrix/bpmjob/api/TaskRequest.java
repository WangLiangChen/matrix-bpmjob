package wang.liangchen.matrix.bpmjob.api;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liangchen.Wang 2023-03-20 14:29
 */
public class TaskRequest {
    private final Long taskId;
    private List<StackTraceElementRequest> stackTrace;

    public TaskRequest(Long taskId, Throwable throwable) {
        this.taskId = taskId;
        if (null == throwable) {
            return;
        }
        StackTraceElement[] throwableStackTrace = throwable.getStackTrace();
        stackTrace = new ArrayList<>(throwableStackTrace.length);
        for (StackTraceElement stackTraceElement : throwableStackTrace) {
            StackTraceElementRequest stackTraceElementRequest = new StackTraceElementRequest();

            stackTrace.add(stackTraceElementRequest);
        }
    }

}
