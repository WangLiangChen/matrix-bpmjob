package wang.liangchen.matrix.bpmjob.domain.task;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import java.time.LocalDateTime;
import java.util.StringJoiner;

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
    private Long taskId;
    /**
     * PrimaryKey
     */
    private Long parentId;
    /**
     * PrimaryKey
     */
    private Long hostId;
    /**
     * PrimaryKey
     */
    private Long triggerId;
    /**
     * 预期分配到的hostLabel
     */
    private String expectedHost;
    /**
     * 实际分配到的hostLabel
     */
    private String actualHost;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    private String taskGroup;
    /**
     * 触发器参数
     */
    private String triggerParams;
    /**
     * 任务参数
     */
    private String taskParams;
    /**
     * 父级任务/上一任务参数
     */
    private String parentParams;
    private Byte shardingNumber;
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
    private LocalDateTime ackDatetime;
    /**
     * 完成时间
     */
    private LocalDateTime completeDatetime;
    /**
     * 进度百分比-乘以100之后的值
     */
    private Short progress;
    /**
     * 状态
     * 状态列
     */
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

    public Long getHostId() {
        return hostId;
    }

    public void setHostId(Long hostId) {
        this.hostId = hostId;
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

    public String getTaskGroup() {
        return taskGroup;
    }

    public void setTaskGroup(String taskGroup) {
        this.taskGroup = taskGroup;
    }

    public String getTriggerParams() {
        return triggerParams;
    }

    public void setTriggerParams(String triggerParams) {
        this.triggerParams = triggerParams;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public String getParentParams() {
        return parentParams;
    }

    public void setParentParams(String parentParams) {
        this.parentParams = parentParams;
    }

    public Byte getShardingNumber() {
        return shardingNumber;
    }

    public void setShardingNumber(Byte shardingNumber) {
        this.shardingNumber = shardingNumber;
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
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Task.class.getSimpleName() + "[", "]")
                .add("taskId=" + taskId)
                .add("parentId=" + parentId)
                .add("hostId=" + hostId)
                .add("triggerId=" + triggerId)
                .add("expectedHost='" + expectedHost + "'")
                .add("actualHost='" + actualHost + "'")
                .add("taskGroup='" + taskGroup + "'")
                .add("triggerParams='" + triggerParams + "'")
                .add("taskParams='" + taskParams + "'")
                .add("parentParams='" + parentParams + "'")
                .add("shardingNumber=" + shardingNumber)
                .add("createDatetime=" + createDatetime)
                .add("assignDatetime=" + assignDatetime)
                .add("ackDatetime=" + ackDatetime)
                .add("completeDatetime=" + completeDatetime)
                .add("progress=" + progress)
                .add("state=" + state)
                .toString();
    }
}