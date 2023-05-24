package liangchen.wang.matrix.bpmjob.sdk.core.thread;

/**
 * @author Liangchen.Wang 2023-05-23 8:06
 */
public class BpmJobThread extends Thread {
    private Long taskId;

    public BpmJobThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
