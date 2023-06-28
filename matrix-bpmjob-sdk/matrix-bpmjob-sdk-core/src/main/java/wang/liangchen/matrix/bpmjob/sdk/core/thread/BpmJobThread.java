package wang.liangchen.matrix.bpmjob.sdk.core.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Liangchen.Wang 2023-05-23 8:06
 */
public class BpmJobThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(BpmJobThread.class);
    private Long taskId;

    public BpmJobThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        this.setUncaughtExceptionHandler((thread, throwable) -> logger.error(thread.getName().concat(" caught exception"), throwable));
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
