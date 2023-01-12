package wang.liangchen.matrix.bpmjob.domain.trigger;

import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;
import wang.liangchen.matrix.framework.ddd.domain.AggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

/**
 * 触发器
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
    @Column(name = "trigger_id")
    private Long triggerId;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    @Column(name = "trigger_group")
    private String triggerGroup;
    /**
     * 名称
     */
    @Column(name = "trigger_name")
    private String triggerName;
    /**
     * 触发器类型:FIXRATE;CRON;
     */
    @Column(name = "trigger_type")
    private String triggerType;
    /**
     * 不同的触发器类型对应的表达式FIXRATE:1S 1M 1H 1D
     */
    @Column(name = "trigger_expression")
    private String triggerExpression;
    /**
     * 错失触发的阈值,单位S
     */
    @Column(name = "miss_threshold")
    private Byte missThreshold;
    /**
     * 触发错失处理策略
     */
    @Column(name = "miss_strategy")
    private MissStrategy missStrategy;
    /**
     * 触发参数
     */
    @Column(name = "trigger_params")
    private String triggerParams;
    /**
     * 任务参数(类型、策略、配置等)
     */
    @Column(name = "task_settings")
    private String taskSettings;
    /**
     * 扩展配置
     */
    @Column(name = "extended_settings")
    private String extendedSettings;
    /**
     * 
     * 版本列
     * 更新和删除时,非空则启用乐观锁
     */
    @Version
    @Column(name = "version")
    private Integer version;
    /**
     * 
     */
    @Column(name = "owner")
    private String owner;
    /**
     * 
     */
    @Column(name = "creator")
    private String creator;
    /**
     * 
     */
    @Column(name = "create_datetime")
    private LocalDateTime createDatetime;
    /**
     * 
     */
    @Column(name = "modifier")
    private String modifier;
    /**
     * 
     */
    @Column(name = "modify_datetime")
    private LocalDateTime modifyDatetime;
    /**
     * 
     */
    @Column(name = "summary")
    private String summary;
    /**
     * 
     * 状态列
     */
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
        if(initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

    public Long getTriggerId() {
        return this.triggerId;
    }
    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }
    public String getTriggerGroup() {
        return this.triggerGroup;
    }
    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }
    public String getTriggerName() {
        return this.triggerName;
    }
    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }
    public String getTriggerType() {
        return this.triggerType;
    }
    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }
    public String getTriggerExpression() {
        return this.triggerExpression;
    }
    public void setTriggerExpression(String triggerExpression) {
        this.triggerExpression = triggerExpression;
    }
    public Byte getMissThreshold() {
        return this.missThreshold;
    }
    public void setMissThreshold(Byte missThreshold) {
        this.missThreshold = missThreshold;
    }
    public MissStrategy getMissStrategy() {
        return this.missStrategy;
    }
    public void setMissStrategy(MissStrategy missStrategy) {
        this.missStrategy = missStrategy;
    }
    public String getTriggerParams() {
        return this.triggerParams;
    }
    public void setTriggerParams(String triggerParams) {
        this.triggerParams = triggerParams;
    }
    public String getTaskSettings() {
        return this.taskSettings;
    }
    public void setTaskSettings(String taskSettings) {
        this.taskSettings = taskSettings;
    }
    public String getExtendedSettings() {
        return this.extendedSettings;
    }
    public void setExtendedSettings(String extendedSettings) {
        this.extendedSettings = extendedSettings;
    }
    public Integer getVersion() {
        return this.version;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
    public String getOwner() {
        return this.owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }
    public String getCreator() {
        return this.creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    public LocalDateTime getCreateDatetime() {
        return this.createDatetime;
    }
    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }
    public String getModifier() {
        return this.modifier;
    }
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
    public LocalDateTime getModifyDatetime() {
        return this.modifyDatetime;
    }
    public void setModifyDatetime(LocalDateTime modifyDatetime) {
        this.modifyDatetime = modifyDatetime;
    }
    public String getSummary() {
        return this.summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public ConstantEnum getState() {
        return this.state;
    }
    public void setState(ConstantEnum state) {
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Trigger{");
        builder.append("triggerId = ").append(triggerId).append(", ");
        builder.append("triggerGroup = ").append(triggerGroup).append(", ");
        builder.append("triggerName = ").append(triggerName).append(", ");
        builder.append("triggerType = ").append(triggerType).append(", ");
        builder.append("triggerExpression = ").append(triggerExpression).append(", ");
        builder.append("missThreshold = ").append(missThreshold).append(", ");
        builder.append("missStrategy = ").append(missStrategy).append(", ");
        builder.append("triggerParams = ").append(triggerParams).append(", ");
        builder.append("taskSettings = ").append(taskSettings).append(", ");
        builder.append("extendedSettings = ").append(extendedSettings).append(", ");
        builder.append("version = ").append(version).append(", ");
        builder.append("owner = ").append(owner).append(", ");
        builder.append("creator = ").append(creator).append(", ");
        builder.append("createDatetime = ").append(createDatetime).append(", ");
        builder.append("modifier = ").append(modifier).append(", ");
        builder.append("modifyDatetime = ").append(modifyDatetime).append(", ");
        builder.append("summary = ").append(summary).append(", ");
        builder.append("state = ").append(state).append(", ");
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }
}