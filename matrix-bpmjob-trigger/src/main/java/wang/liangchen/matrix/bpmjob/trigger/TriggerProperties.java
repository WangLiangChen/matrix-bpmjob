package wang.liangchen.matrix.bpmjob.trigger;

import wang.liangchen.matrix.framework.commons.network.NetUtil;

import java.time.Duration;
import java.util.Properties;

/**
 * @author Liangchen.Wang 2023-05-10 7:47
 */
public class TriggerProperties extends Properties {
    private String hostLabel = NetUtil.INSTANCE.getLocalHostName();
    private Duration acquireTriggerDuration = Duration.ofSeconds(10);
    private Duration missedThreshold = Duration.ofSeconds(5);
    private Short batchSize = 100;
    private Byte threadNumber = 64;

    public String getHostLabel() {
        return hostLabel;
    }

    public void setHostLabel(String hostLabel) {
        this.hostLabel = hostLabel;
    }

    public Duration getAcquireTriggerDuration() {
        return acquireTriggerDuration;
    }

    public void setAcquireTriggerDuration(Duration acquireTriggerDuration) {
        this.acquireTriggerDuration = acquireTriggerDuration;
    }

    public Duration getMissedThreshold() {
        return missedThreshold;
    }

    public void setMissedThreshold(Duration missedThreshold) {
        this.missedThreshold = missedThreshold;
    }

    public Short getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Short batchSize) {
        this.batchSize = batchSize;
    }

    public Byte getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(Byte threadNumber) {
        this.threadNumber = threadNumber;
    }
}
