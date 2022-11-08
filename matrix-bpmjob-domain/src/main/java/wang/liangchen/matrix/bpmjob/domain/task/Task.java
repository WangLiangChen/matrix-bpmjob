package wang.liangchen.matrix.bpmjob.domain.task;

import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;
import wang.liangchen.matrix.framework.ddd.domain.AggregateRoot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * 触发任务
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@AggregateRoot
@Entity(name = "bpmjob_task")
public class Task extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    @Column(name = "task_id")
    private Long taskId;
    /**
     * PrimaryKey
     */
    @Column(name = "parent_id")
    private Long parentId;
    /**
     * PrimaryKey
     */
    @Column(name = "host_id")
    private Long hostId;
    /**
     * PrimaryKey
     */
    @Column(name = "trigger_id")
    private Long triggerId;
    /**
     * PrimaryKey
     */
    @Column(name = "wal_id")
    private Long walId;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    @Column(name = "task_group")
    private String taskGroup;
    /**
     * 触发器参数
     */
    @Column(name = "trigger_params")
    private String triggerParams;
    /**
     * 任务参数
     */
    @Column(name = "task_params")
    private String taskParams;
    /**
     * 父级任务/上一任务参数
     */
    @Column(name = "parent_params")
    private String parentParams;
    /**
     * 创建时间
     */
    @Column(name = "create_datetime")
    private LocalDateTime createDatetime;
    /**
     * 分配时间
     */
    @Column(name = "assign_datetime")
    private LocalDateTime assignDatetime;
    /**
     * 分配后,消费端确认时间
     */
    @Column(name = "ack_datetime")
    private LocalDateTime ackDatetime;
    /**
     * 完成时间
     */
    @Column(name = "complete_datetime")
    private LocalDateTime completeDatetime;
    /**
     * 进度百分比-乘以100之后的值
     */
    @Column(name = "progress")
    private Short progress;
    /**
     * 状态
     * 状态列
     */
    @ColumnState
    @Column(name = "state")
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
        return this.taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getHostId() {
        return this.hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }

    public Long getTriggerId() {
        return this.triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public Long getWalId() {
        return this.walId;
    }

    public void setWalId(Long walId) {
        this.walId = walId;
    }

    public String getTaskGroup() {
        return this.taskGroup;
    }

    public void setTaskGroup(String taskGroup) {
        this.taskGroup = taskGroup;
    }

    public String getTriggerParams() {
        return this.triggerParams;
    }

    public void setTriggerParams(String triggerParams) {
        this.triggerParams = triggerParams;
    }

    public String getTaskParams() {
        return this.taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public String getParentParams() {
        return this.parentParams;
    }

    public void setParentParams(String parentParams) {
        this.parentParams = parentParams;
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

    public LocalDateTime getAckDatetime() {
        return ackDatetime;
    }

    public void setAckDatetime(LocalDateTime ackDatetime) {
        this.ackDatetime = ackDatetime;
    }

    public LocalDateTime getCompleteDatetime() {
        return completeDatetime;
    }

    public void setCompleteDatetime(LocalDateTime completeDatetime) {
        this.completeDatetime = completeDatetime;
    }

    public Short getProgress() {
        return progress;
    }

    public void setProgress(Short progress) {
        this.progress = progress;
    }

    public Byte getState() {
        return this.state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Task{");
        builder.append("taskId = ").append(taskId).append(", ");
        builder.append("parentId = ").append(parentId).append(", ");
        builder.append("hostId = ").append(hostId).append(", ");
        builder.append("triggerId = ").append(triggerId).append(", ");
        builder.append("walId = ").append(walId).append(", ");
        builder.append("taskGroup = ").append(taskGroup).append(", ");
        builder.append("triggerParams = ").append(triggerParams).append(", ");
        builder.append("taskParams = ").append(taskParams).append(", ");
        builder.append("parentParams = ").append(parentParams).append(", ");
        builder.append("state = ").append(state).append(", ");
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }
}