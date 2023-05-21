package wang.liangchen.matrix.bpmjob.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.*;
import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;
import wang.liangchen.matrix.framework.commons.validation.InsertGroup;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;

/**
 * @author Liangchen.Wang 2023-01-16 14:27
 */
public class TriggerRequest {
    private Long triggerId;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    @NotBlank(groups = InsertGroup.class)
    private String triggerGroup;
    /**
     * 名称
     */
    @NotBlank(groups = InsertGroup.class)
    private String triggerName;
    /**
     * 触发器类型:FIXRATE;CRON;
     */
    @NotNull(groups = InsertGroup.class)
    private TriggerType triggerType;
    /**
     * 不同的触发器类型对应的表达式
     */
    @NotBlank(groups = InsertGroup.class)
    private String triggerCron;

    /**
     * 执行器类型：JAVA_EXECUTOR;SCRIPT_EXECUTOR
     */
    @NotNull(groups = InsertGroup.class)
    private ExecutorType executorType;
    /**
     * 不同的执行器对应的配置
     */
    private String executorOption;
    /**
     * 错失触发的阈值,单位S
     */
    @NotNull(groups = InsertGroup.class)
    private Byte missThreshold;
    /**
     * 触发错失处理策略
     */
    @NotNull(groups = InsertGroup.class)
    private MissedStrategy missedStrategy;
    /**
     * 任务分配策略
     */
    @NotNull(groups = InsertGroup.class)
    private AssignStrategy assignStrategy;
    /**
     * 任务分片策略AUTO-自动;PROGRAM-编程式;
     */
    @NotNull(groups = InsertGroup.class)
    private ShardingStrategy shardingStrategy;
    /**
     * 分片数，子任务数；0-不分片，不能创建子任务
     */
    @NotNull(groups = InsertGroup.class)
    private Byte shardingNumber;
    /**
     * 触发参数
     */
    @NotNull(groups = InsertGroup.class)
    private JsonField triggerParams;

    private String owner;

    private String creator;

    private String modifier;
    private String summary;
    private ConstantEnum state;

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerCron() {
        return triggerCron;
    }

    public void setTriggerCron(String triggerCron) {
        this.triggerCron = triggerCron;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }

    public String getExecutorOption() {
        return executorOption;
    }

    public void setExecutorOption(String executorOption) {
        this.executorOption = executorOption;
    }

    public Byte getMissThreshold() {
        return missThreshold;
    }

    public void setMissThreshold(Byte missThreshold) {
        this.missThreshold = missThreshold;
    }

    public MissedStrategy getMissStrategy() {
        return missedStrategy;
    }

    public void setMissStrategy(MissedStrategy missedStrategy) {
        this.missedStrategy = missedStrategy;
    }

    public AssignStrategy getAssignStrategy() {
        return assignStrategy;
    }

    public void setAssignStrategy(AssignStrategy assignStrategy) {
        this.assignStrategy = assignStrategy;
    }

    public ShardingStrategy getShardingStrategy() {
        return shardingStrategy;
    }

    public void setShardingStrategy(ShardingStrategy shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
    }

    public Byte getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Byte shardingNumber) {
        this.shardingNumber = shardingNumber;
    }

    public JsonField getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(JsonField triggerParams) {
        this.triggerParams = triggerParams;
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

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ConstantEnum getState() {
        return state;
    }

    public void setState(ConstantEnum state) {
        this.state = state;
    }
}
