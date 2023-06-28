package wang.liangchen.matrix.bpmjob.sdk.core.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Liangchen.Wang 2023-05-21 16:40
 */
public class BpmJobThreadFactory implements ThreadFactory {
    private final static Logger logger = LoggerFactory.getLogger(BpmJobThreadFactory.class);
    // 按ThreadGroupName计数
    private final static ConcurrentHashMap<String, AtomicInteger> poolCounter = new ConcurrentHashMap<>();
    private final String poolName;
    private final ThreadGroup threadGroup;
    private final AtomicInteger threadCounter;

    public BpmJobThreadFactory(String poolName, String threadGroupName) {
        this.poolName = poolName;
        this.threadGroup = new ThreadGroup(threadGroupName);
        this.threadCounter = poolCounter.computeIfAbsent(threadGroupName, key -> new AtomicInteger());
    }

    @Override
    public Thread newThread(Runnable runnable) {
        return new BpmJobThread(this.threadGroup, runnable, String.format("%s-%d", this.poolName, this.threadCounter.getAndIncrement()));
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
