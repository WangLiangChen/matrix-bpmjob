package wang.liangchen.matrix.bpmjob.trigger.domain.trigger;

import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnJson;
import wang.liangchen.matrix.framework.data.annotation.ColumnMarkDelete;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;
import wang.liangchen.matrix.framework.ddd.domain.AggregateRoot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.time.LocalDateTime;

/**
 * 触发器
 *
 * @author Liangchen.Wang 2022-10-26 09:04:57
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
     * 名称
     */
    @Column(name = "trigger_name")
    private String triggerName;
    /**
     * 所属组,触发器划分的维度
     */
    @Column(name = "trigger_group")
    private String triggerGroup;
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
     * 末次实际触发时间
     */
    @Column(name = "trigger_last")
    private LocalDateTime triggerLast;
    /**
     * 下次预期触发时间
     */
    @Column(name = "trigger_next")
    private LocalDateTime triggerNext;
    /**
     * 触发错失阈值,单位S
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
     * 任务阻塞策略
     */
    @Column(name = "block_strategy")
    private String blockStrategy;
    /**
     * 扩展配置
     * 对象和JSON格式自动互转列
     * 非基本类型需实现Serializable接口以避免代码错误提示
     */
    @ColumnJson
    @Column(name = "extended_settings")
    private String extendedSettings;
    /**
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
     * 逻辑删除列和值
     * 状态列
     */
    @ColumnMarkDelete("DELETED")
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

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
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

    public LocalDateTime getTriggerLast() {
        return triggerLast;
    }

    public void setTriggerLast(LocalDateTime triggerLast) {
        this.triggerLast = triggerLast;
    }

    public LocalDateTime getTriggerNext() {
        return triggerNext;
    }

    public void setTriggerNext(LocalDateTime triggerNext) {
        this.triggerNext = triggerNext;
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

    public String getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(String triggerParams) {
        this.triggerParams = triggerParams;
    }

    public String getBlockStrategy() {
        return blockStrategy;
    }

    public void setBlockStrategy(String blockStrategy) {
        this.blockStrategy = blockStrategy;
    }

    public String getExtendedSettings() {
        return extendedSettings;
    }

    public void setExtendedSettings(String extendedSettings) {
        this.extendedSettings = extendedSettings;
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
        StringBuilder builder = new StringBuilder();
        builder.append("Trigger{");
        builder.append("triggerId = ").append(triggerId).append(", ");
        builder.append("triggerName = ").append(triggerName).append(", ");
        builder.append("triggerGroup = ").append(triggerGroup).append(", ");
        builder.append("triggerType = ").append(triggerType).append(", ");
        builder.append("triggerExpression = ").append(triggerExpression).append(", ");
        builder.append("triggerLast = ").append(triggerLast).append(", ");
        builder.append("triggerNext = ").append(triggerNext).append(", ");
        builder.append("triggerParams = ").append(triggerParams).append(", ");
        builder.append("missStrategy = ").append(missStrategy).append(", ");
        builder.append("blockStrategy = ").append(blockStrategy).append(", ");
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