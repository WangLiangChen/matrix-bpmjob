package wang.liangchen.matrix.bpmjob.trigger.domain.trigger;

import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2023-07-27 22:00
 */
public class Wal {
    private Long walId;
    private Long triggerId;
    private String walKey;
    private String hostLabel;
    private String taskParams;
    private LocalDateTime expectedDatetime;
    private LocalDateTime createDatetime;
    private Byte state;

    public Long getWalId() {
        return walId;
    }

    public void setWalId(Long walId) {
        this.walId = walId;
    }

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public String getWalKey() {
        return walKey;
    }

    public void setWalKey(String walKey) {
        this.walKey = walKey;
    }

    public String getHostLabel() {
        return hostLabel;
    }

    public void setHostLabel(String hostLabel) {
        this.hostLabel = hostLabel;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public LocalDateTime getExpectedDatetime() {
        return expectedDatetime;
    }

    public void setExpectedDatetime(LocalDateTime expectedDatetime) {
        this.expectedDatetime = expectedDatetime;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}
