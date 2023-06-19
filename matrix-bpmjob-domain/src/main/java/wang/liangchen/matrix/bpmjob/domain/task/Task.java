package wang.liangchen.matrix.bpmjob.domain.task;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.ExecutorType;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import java.time.LocalDateTime;

/**
 * 触发任务
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@Entity(name = "bpmjob_task")
public class Task extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(value = IdStrategy.Strategy.MatrixFlake)
    private Long taskId;
    /**
     * PrimaryKey
     */
    private Long parentId;
    /**
     * PrimaryKey
     */
    private Long walId;
    /**
     * PrimaryKey
     */
    private Long triggerId;
    /**
     * 预期分配到任务的executor
     */
    private String expectedHost;
    /**
     * 实际分配到任务的executor
     */
    private String actualHost;
    private String tenantCode;
    private String appCode;
    /**
     * 执行器类型：JAVA_EXECUTOR;SCRIPT_EXECUTOR
     */
    private ExecutorType executorType;
    /**
     * 执行器选项 JAVA_EXECUTOR:CLASS,METHOD;SCRIPT_EXECUTOR:GROOVY,PYTHON,SHELL,PHP,NODEJS,POWERSHELL
     */
    private String executorOption;
    /**
     * 配置在trigger上的参数-静态
     */
    private JsonField triggerParams;
    /**
     * 显式创建任务的参数-动态,会覆盖合并trigger_params.如API触发、子任务、流程任务等
     */
    private JsonField taskParams;
    /**
     * 分片数;子任务数;0-不分片,不能创建子任务
     */
    private Byte shardingNumber;
    /**
     * 预期触发时间
     */
    private LocalDateTime expectedDatetime;
    /**
     * 创建时间
     */
    private LocalDateTime createDatetime;
    /**
     * 分配时间
     */
    private LocalDateTime assignDatetime;
    /**
     * 分配后,消费端确认时间
     */
    private LocalDateTime acceptDatetime;
    /**
     * 完成时间
     */
    private LocalDateTime completeDatetime;
    /**
     * 完成信息摘要-正常/异常
     */
    private String completeSummary;
    /**
     * 进度百分比-乘以100之后的值
     */
    private Byte progress;
    /**
     * 状态
     * 状态列
     */
    private Byte state;

    public static Task valueOf(Object source) {
        return ObjectUtil.INSTANCE.copyProperties(source, Task.class);
    }

    public static Task newInstance() {
        return ClassUtil.INSTANCE.instantiate(Task.class);
    }

    public static Task newInstance(boolean initializeFields) {
        Task entity = ClassUtil.INSTANCE.instantiate(Task.class);
        if (initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

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

    public String getExpectedHost() {
        return expectedHost;
    }

    public void setExpectedHost(String expectedHost) {
        this.expectedHost = expectedHost;
    }

    public String getActualHost() {
        return actualHost;
    }

    public void setActualHost(String actualHost) {
        this.actualHost = actualHost;
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

    public JsonField getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(JsonField taskParams) {
        this.taskParams = taskParams;
    }

    public Byte getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Byte shardingNumber) {
        this.shardingNumber = shardingNumber;
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


    public LocalDateTime getAssignDatetime() {
        return assignDatetime;
    }

    public void setAssignDatetime(LocalDateTime assignDatetime) {
        this.assignDatetime = assignDatetime;
    }

    public LocalDateTime getAcceptDatetime() {
        return acceptDatetime;
    }

    public void setAcceptDatetime(LocalDateTime acceptDatetime) {
        this.acceptDatetime = acceptDatetime;
    }

    public LocalDateTime getCompleteDatetime() {
        return completeDatetime;
    }

    public void setCompleteDatetime(LocalDateTime completeDatetime) {
        this.completeDatetime = completeDatetime;
    }

    public String getCompleteSummary() {
        return completeSummary;
    }

    public void setCompleteSummary(String completeSummary) {
        this.completeSummary = completeSummary;
    }

    public Byte getProgress() {
        return progress;
    }

    public void setProgress(Byte progress) {
        this.progress = progress;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}