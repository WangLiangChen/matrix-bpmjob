package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.*;
import wang.liangchen.matrix.framework.commons.enumeration.ConstantEnum;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnJson;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import java.time.LocalDateTime;

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
    private TriggerType triggerType;
    /**
     * 不同的触发器类型对应的表达式
     */
    private String triggerCron;

    /**
     * 执行器类型：JAVA_EXECUTOR;SCRIPT_EXECUTOR
     */
    private ExecutorType executorType;
    /**
     * 执行器选项 JAVA_EXECUTOR:CLASS,METHOD;SCRIPT_EXECUTOR:GROOVY,PYTHON,SHELL,PHP,NODEJS,POWERSHELL
     */
    private String executorOption;
    /**
     * 触发参数
     */
    @ColumnJson
    private JsonField triggerParams;
    /**
     * 分片数，子任务数；0-不分片，不能创建子任务
     */
    private Byte shardingNumber;
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
    private ShardingStrategy shardingStrategy;


    /**
     * 扩展配置
     */
    @ColumnJson
    private JsonField extendedSettings;
    /**
     * 任务参数(类型、策略、配置等)
     */
    @ColumnJson
    private JsonField taskSettings;

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

    public JsonField getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(JsonField triggerParams) {
        this.triggerParams = triggerParams;
    }

    public Byte getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Byte shardingNumber) {
        this.shardingNumber = shardingNumber;
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

    public ShardingStrategy getShardingStrategy() {
        return shardingStrategy;
    }

    public void setShardingStrategy(ShardingStrategy shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
    }

    public JsonField getExtendedSettings() {
        return extendedSettings;
    }

    public void setExtendedSettings(JsonField extendedSettings) {
        this.extendedSettings = extendedSettings;
    }

    public JsonField getTaskSettings() {
        return taskSettings;
    }

    public void setTaskSettings(JsonField taskSettings) {
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
}