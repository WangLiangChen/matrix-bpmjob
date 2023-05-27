package liangchen.wang.matrix.bpmjob.sdk.core.client;

import liangchen.wang.matrix.bpmjob.sdk.core.BpmJobSdkProperties;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public class TaskClient {
    private final BpmJobSdkProperties bpmJobSdkProperties;

    public TaskClient(BpmJobSdkProperties bpmJobSdkProperties) {
        this.bpmJobSdkProperties = bpmJobSdkProperties;
    }

    public List<TaskResponse> getTasks(int number) {
        // 不处理异常
        return Collections.emptyList();
    }

    public void acceptTasks(Set<Long> taskIds) {

    }

    public void completeTask(Long taskId, Exception exception) {

    }

}
