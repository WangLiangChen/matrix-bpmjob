package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.AssignStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.SharingStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.fields.ExtendedSettings;
import wang.liangchen.matrix.bpmjob.domain.trigger.fields.TaskSettings;
import wang.liangchen.matrix.bpmjob.domain.trigger.fields.TriggerParams;
import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;
import wang.liangchen.matrix.framework.ddd.domain.AggregateRoot;

import java.time.LocalDateTime;

/**
 * 触发器
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@AggregateRoot
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
     * 触发器类型:API;CRONFIX;RATE;FIXDELAY;
     */
    private String triggerType;
    /**
     * 不同的触发器类型对应的表达式FIXRATE:1S 1M 1H 1D
     */
    private String triggerExpression;

    /**
     * 执行器类型:JAVA_EXECUTOR;SCRIPT_EXECUTOR
     */
    private String executor_type;
    /**
     * 执行器配置 不同的执行器对应不同的配置
     */
    private String executor_settings;
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
     * 分片策略
     */
    private SharingStrategy sharingStrategy;
    /**
     * 分片数；0-不分片和不允许创建子任务
     */
    private Short shardingNumber;
    /**
     * 触发参数
     */
    private TriggerParams triggerParams;
    /**
     * 扩展配置
     */
    private ExtendedSettings extendedSettings;
    /**
     * 任务扩展配置(类型、策略、配置等)
     */
    private TaskSettings taskSettings;

    /**
     * 下次预期触发时间
     */
    private LocalDateTime triggerNext;
    /**
     * 版本列
     * 更新和删除时,非空则启用乐观锁
     */
    @Version
    private Integer version;

    private String owner;

    private String creator;

    private LocalDateTime createDatetime;

    private String modifier;

    private LocalDateTime modifyDatetime;

    private String summary;
    @ColumnState
    @Column(name = "state")
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

    public String getExecutor_type() {
        return executor_type;
    }

    public void setExecutor_type(String executor_type) {
        this.executor_type = executor_type;
    }

    public String getExecutor_settings() {
        return executor_settings;
    }

    public void setExecutor_settings(String executor_settings) {
        this.executor_settings = executor_settings;
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

    public Short getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Short shardingNumber) {
        this.shardingNumber = shardingNumber;
    }

    public TriggerParams getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(TriggerParams triggerParams) {
        this.triggerParams = triggerParams;
    }

    public ExtendedSettings getExtendedSettings() {
        return extendedSettings;
    }

    public void setExtendedSettings(ExtendedSettings extendedSettings) {
        this.extendedSettings = extendedSettings;
    }

    public TaskSettings getTaskSettings() {
        return taskSettings;
    }

    public void setTaskSettings(TaskSettings taskSettings) {
        this.taskSettings = taskSettings;
    }

    public LocalDateTime getTriggerNext() {
        return triggerNext;
    }

    public void setTriggerNext(LocalDateTime triggerNext) {
        this.triggerNext = triggerNext;
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
}