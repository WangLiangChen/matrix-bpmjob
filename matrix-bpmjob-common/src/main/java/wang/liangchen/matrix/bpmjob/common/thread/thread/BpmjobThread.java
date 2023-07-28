package wang.liangchen.matrix.bpmjob.common.thread.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Liangchen.Wang 2023-05-23 8:06
 */
public class BpmjobThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(BpmjobThread.class);

    public BpmjobThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        this.setUncaughtExceptionHandler((thread, throwable) -> logger.error("The thread '{}' caught exception.", thread.getName(), throwable));
    }

}
