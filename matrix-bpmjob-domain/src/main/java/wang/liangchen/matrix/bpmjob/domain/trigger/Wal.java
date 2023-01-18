package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.JsonField;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import java.time.LocalDateTime;

/**
 * Write-Ahead Logging
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@Entity(name = "bpmjob_wal")
public class Wal extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    private Long walId;
    /**
     * PrimaryKey
     */
    private Long hostId;
    /**
     * PrimaryKey
     */
    private Long triggerId;
    private String hostLabel;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    private String walGroup;
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
     * 创建时间
     */
    private LocalDateTime createDatetime;
    /**
     * 实际调度时间
     */
    private LocalDateTime scheduleDatetime;
    /**
     * 预期触发时间
     */
    private LocalDateTime triggerDatetime;
    /**
     * 1-acquired;2-triggered
     * 状态列
     */
    private Byte state;

    public static Wal valueOf(Object source) {
        return ObjectUtil.INSTANCE.copyProperties(source, Wal.class);
    }

    public static Wal newInstance() {
        return ClassUtil.INSTANCE.instantiate(Wal.class);
    }

    public static Wal newInstance(boolean initializeFields) {
        Wal entity = ClassUtil.INSTANCE.instantiate(Wal.class);
        if (initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

    public Long getWalId() {
        return walId;
    }

    public void setWalId(Long walId) {
        this.walId = walId;
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

    public String getHostLabel() {
        return hostLabel;
    }

    public void setHostLabel(String hostLabel) {
        this.hostLabel = hostLabel;
    }

    public String getWalGroup() {
        return walGroup;
    }

    public void setWalGroup(String walGroup) {
        this.walGroup = walGroup;
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

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }

    public LocalDateTime getScheduleDatetime() {
        return scheduleDatetime;
    }

    public void setScheduleDatetime(LocalDateTime scheduleDatetime) {
        this.scheduleDatetime = scheduleDatetime;
    }

    public LocalDateTime getTriggerDatetime() {
        return triggerDatetime;
    }

    public void setTriggerDatetime(LocalDateTime triggerDatetime) {
        this.triggerDatetime = triggerDatetime;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}