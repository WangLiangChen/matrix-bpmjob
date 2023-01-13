package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.AssignStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.SharingStrategy;
import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * 触发器
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@Entity(name = "bpmjob_trigger")
public class Trigger extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    private Long triggerId;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    private String triggerGroup;
    /**
     * 名称
     */
    private String triggerName;
    /**
     * 触发器类型:FIXRATE;CRON;
     */
    private String triggerType;
    /**
     * 不同的触发器类型对应的表达式FIXRATE:1S 1M 1H 1D
     */
    private String triggerExpression;

    /**
     * 执行器类型：JAVA_EXECUTOR;SCRIPT_EXECUTOR
     */
    private String executorType;
    /**
     * 不同的执行器对应的配置
     */
    private String executorSettings;
    /**
     * 错失触发的阈值,单位S
     */
    private Byte missThreshold;
    /**
     * 触发错失处理策略
     */
    private MissStrategy missStrategy;

    /**
     * 任务分配策略
     */
    private AssignStrategy assignStrategy;
    /**
     * 任务分片策略AUTO-自动;PROGRAM-编程式;
     */
    private SharingStrategy sharingStrategy;
    /**
     * 分片数，子任务数；0-不分片，不能创建子任务
     */
    private Byte shardingNumber;
    /**
     * 触发参数
     */
    private String triggerParams;
    /**
     * 扩展配置
     */
    private String extendedSettings;
    /**
     * 任务参数(类型、策略、配置等)
     */
    private String taskSettings;

    /**
     * 版本列
     * 更新和删除时,非空则启用乐观锁
     */
    @Version
    private Integer version;
    /**
     *
     */
    private String owner;
    /**
     *
     */
    private String creator;
    /**
     *
     */
    private LocalDateTime createDatetime;
    /**
     *
     */
    private String modifier;
    /**
     *
     */
    private LocalDateTime modifyDatetime;
    /**
     *
     */
    private String summary;
    /**
     * 状态列
     */
    @ColumnState
    private ConstantEnum state;

    public static Trigger valueOf(Object source) {
        return ObjectUtil.INSTANCE.copyProperties(source, Trigger.class);
    }

    public static Trigger newInstance() {
        return ClassUtil.INSTANCE.instantiate(Trigger.class);
    }

    public static Trigger newInstance(boolean initializeFields) {
        Trigger entity = ClassUtil.INSTANCE.instantiate(Trigger.class);
        if (initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

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

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerExpression() {
        return triggerExpression;
    }

    public void setTriggerExpression(String triggerExpression) {
        this.triggerExpression = triggerExpression;
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

    public Byte getMissThreshold() {
        return missThreshold;
    }

    public void setMissThreshold(Byte missThreshold) {
        this.missThreshold = missThreshold;
    }

    public MissStrategy getMissStrategy() {
        return missStrategy;
    }

    public void setMissStrategy(MissStrategy missStrategy) {
        this.missStrategy = missStrategy;
    }

    public AssignStrategy getAssignStrategy() {
        return assignStrategy;
    }

    public void setAssignStrategy(AssignStrategy assignStrategy) {
        this.assignStrategy = assignStrategy;
    }

    public SharingStrategy getSharingStrategy() {
        return sharingStrategy;
    }

    public void setSharingStrategy(SharingStrategy sharingStrategy) {
        this.sharingStrategy = sharingStrategy;
    }

    public Byte getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Byte shardingNumber) {
        this.shardingNumber = shardingNumber;
    }

    public String getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(String triggerParams) {
        this.triggerParams = triggerParams;
    }

    public String getExtendedSettings() {
        return extendedSettings;
    }

    public void setExtendedSettings(String extendedSettings) {
        this.extendedSettings = extendedSettings;
    }

    public String getTaskSettings() {
        return taskSettings;
    }

    public void setTaskSettings(String taskSettings) {
        this.taskSettings = taskSettings;
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

    public ConstantEnum getState() {
        return state;
    }

    public void setState(ConstantEnum state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Trigger.class.getSimpleName() + "[", "]")
                .add("triggerId=" + triggerId)
                .add("triggerGroup='" + triggerGroup + "'")
                .add("triggerName='" + triggerName + "'")
                .add("triggerType='" + triggerType + "'")
                .add("triggerExpression='" + triggerExpression + "'")
                .add("executorType='" + executorType + "'")
                .add("executorSettings='" + executorSettings + "'")
                .add("missThreshold=" + missThreshold)
                .add("missStrategy=" + missStrategy)
                .add("assignStrategy=" + assignStrategy)
                .add("sharingStrategy=" + sharingStrategy)
                .add("shardingNumber=" + shardingNumber)
                .add("triggerParams='" + triggerParams + "'")
                .add("extendedSettings='" + extendedSettings + "'")
                .add("taskSettings='" + taskSettings + "'")
                .add("version=" + version)
                .add("owner='" + owner + "'")
                .add("creator='" + creator + "'")
                .add("createDatetime=" + createDatetime)
                .add("modifier='" + modifier + "'")
                .add("modifyDatetime=" + modifyDatetime)
                .add("summary='" + summary + "'")
                .add("state=" + state)
                .toString();
    }
}