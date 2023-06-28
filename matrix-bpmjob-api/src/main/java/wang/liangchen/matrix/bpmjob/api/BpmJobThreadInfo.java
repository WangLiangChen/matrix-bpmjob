package wang.liangchen.matrix.bpmjob.api;

import java.lang.management.LockInfo;
import java.lang.management.ThreadInfo;

/**
 * @author Liangchen.Wang 2023-05-21 17:21
 */
public class BpmJobThreadInfo {
    private final ThreadInfo threadInfo;
    private Long taskId;
    private long threadId;
    private String threadName;
    private Thread.State threadState;
    private String lockClassName;
    private int lockHashCode;
    private String lockName;
    private long lockOwnerId;
    private String lockOwnerName;
    private long blockedTime;
    private long blockedCount;
    private long waitedTime;
    private long waitedCount;

    public BpmJobThreadInfo(ThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
        if (null == this.threadInfo) {
            return;
        }
        this.threadId = threadInfo.getThreadId();
        this.threadName = threadInfo.getThreadName();
        this.threadState = threadInfo.getThreadState();
        LockInfo lockInfo = threadInfo.getLockInfo();
        if (null != lockInfo) {
            this.lockClassName = lockInfo.getClassName();
            this.lockHashCode = lockInfo.getIdentityHashCode();
        }
        this.lockName = threadInfo.getLockName();
        this.lockOwnerId = threadInfo.getLockOwnerId();
        this.lockOwnerName = threadInfo.getLockOwnerName();
        this.blockedTime = threadInfo.getBlockedTime();
        this.blockedCount = threadInfo.getBlockedCount();
        this.waitedTime = threadInfo.getWaitedTime();
        this.waitedCount = threadInfo.getWaitedCount();
    }

    public BpmJobThreadInfo() {
        this(null);
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Thread.State getThreadState() {
        return threadState;
    }

    public void setThreadState(Thread.State threadState) {
        this.threadState = threadState;
    }

    public String getLockClassName() {
        return lockClassName;
    }

    public void setLockClassName(String lockClassName) {
        this.lockClassName = lockClassName;
    }

    public int getLockHashCode() {
        return lockHashCode;
    }

    public void setLockHashCode(int lockHashCode) {
        this.lockHashCode = lockHashCode;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
    }

    public long getLockOwnerId() {
        return lockOwnerId;
    }

    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }

    public String getLockOwnerName() {
        return lockOwnerName;
    }

    public void setLockOwnerName(String lockOwnerName) {
        this.lockOwnerName = lockOwnerName;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public void setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public long getWaitedTime() {
        return waitedTime;
    }

    public void setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
    }

    public long getWaitedCount() {
        return waitedCount;
    }

    public void setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
    }
}
