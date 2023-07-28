package wang.liangchen.matrix.bpmjob.trigger.domain.trigger;

import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2023-07-27 22:01
 */
public class Trigger {
    private Long triggerId;
    private String tenantCode;
    private String appCode;
    private String triggerName;
    private String triggerType;
    private String triggerCron;
    private String executorType;
    private String executorSettings;
    private String scriptCode;
    private String triggerParams;
    private Byte missedThreshold;
    private String missedStrategy;
    private String assignStrategy;
    private Byte shardingNumber;
    private Integer runningDurationThreshold;
    private Integer version;
    private String owner;
    private String creator;
    private LocalDateTime createDatetime;
    private String modifier;
    private LocalDateTime modifyDatetime;
    private String summary;
    private String state;

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerCron() {
        return triggerCron;
    }

    public void setTriggerCron(String triggerCron) {
        this.triggerCron = triggerCron;
    }

    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String executorType) {
        this.executorType = executorType;
    }

    public String getExecutorSettings() {
        return executorSettings;
    }

    public void setExecutorSettings(String executorSettings) {
        this.executorSettings = executorSettings;
    }

    public String getScriptCode() {
        return scriptCode;
    }

    public void setScriptCode(String scriptCode) {
        this.scriptCode = scriptCode;
    }

    public String getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(String triggerParams) {
        this.triggerParams = triggerParams;
    }

    public Byte getMissedThreshold() {
        return missedThreshold;
    }

    public void setMissedThreshold(Byte missedThreshold) {
        this.missedThreshold = missedThreshold;
    }

    public String getMissedStrategy() {
        return missedStrategy;
    }

    public void setMissedStrategy(String missedStrategy) {
        this.missedStrategy = missedStrategy;
    }

    public String getAssignStrategy() {
        return assignStrategy;
    }

    public void setAssignStrategy(String assignStrategy) {
        this.assignStrategy = assignStrategy;
    }

    public Byte getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Byte shardingNumber) {
        this.shardingNumber = shardingNumber;
    }

    public Integer getRunningDurationThreshold() {
        return runningDurationThreshold;
    }

    public void setRunningDurationThreshold(Integer runningDurationThreshold) {
        this.runningDurationThreshold = runningDurationThreshold;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public LocalDateTime getModifyDatetime() {
        return modifyDatetime;
    }

    public void setModifyDatetime(LocalDateTime modifyDatetime) {
        this.modifyDatetime = modifyDatetime;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
