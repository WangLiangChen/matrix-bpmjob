package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.*;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnJson;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.CommonEntity;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;

/**
 * 触发器
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@Entity(name = "bpmjob_trigger")
public class Trigger extends CommonEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    private Long triggerId;
    private String tenantCode;
    private String appCode;
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
    private Byte missedThreshold;
    /**
     * 触发错失处理策略
     */
    private MissedStrategy missedStrategy;

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

    public Byte getMissedThreshold() {
        return missedThreshold;
    }

    public void setMissedThreshold(Byte missedThreshold) {
        this.missedThreshold = missedThreshold;
    }

    public MissedStrategy getMissedStrategy() {
        return missedStrategy;
    }

    public void setMissedStrategy(MissedStrategy missedStrategy) {
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
}