package wang.liangchen.matrix.bpmjob.sdk.core.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Liangchen.Wang 2023-05-21 13:46
 */
public enum ThreadSnapshot {
    INSTANCE;
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    ThreadSnapshot() {
        if (this.threadMXBean.isThreadContentionMonitoringSupported()) {
            this.threadMXBean.setThreadContentionMonitoringEnabled(true);
        }
    }

    public List<ThreadInfo> threadInfo(ThreadGroup threadGroup) {
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
        return Arrays.stream(threads).map(this::threadInfo).collect(Collectors.toList());
    }

    public ThreadInfo threadInfo(Thread thread) {
        return threadInfo(thread.getId());
    }

    public ThreadInfo threadInfo(long threadId) {
        return this.threadMXBean.getThreadInfo(threadId, Integer.MAX_VALUE);
    }
}
