package wang.liangchen.matrix.bpmjob.domain.trigger;

import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    @Column(name = "wal_id")
    private Long walId;
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
     * 创建时间
     */
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    @Column(name = "wal_group")
    private String walGroup;
    @Column(name = "create_datetime")
    private LocalDateTime createDatetime;
    /**
     * 预期触发时间
     */
    @Column(name = "schedule_datetime")
    private LocalDateTime scheduleDatetime;
    /**
     * 实际触发时间
     */
    @Column(name = "trigger_datetime")
    private LocalDateTime triggerDatetime;
    /**
     * 1-acquired;2-triggered
     * 状态列
     */
    @ColumnState
    @Column(name = "state")
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
        return this.walId;
    }

    public void setWalId(Long walId) {
        this.walId = walId;
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

    public String getWalGroup() {
        return walGroup;
    }

    public void setWalGroup(String walGroup) {
        this.walGroup = walGroup;
    }

    public LocalDateTime getCreateDatetime() {
        return this.createDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }

    public LocalDateTime getScheduleDatetime() {
        return this.scheduleDatetime;
    }

    public void setScheduleDatetime(LocalDateTime scheduleDatetime) {
        this.scheduleDatetime = scheduleDatetime;
    }

    public LocalDateTime getTriggerDatetime() {
        return this.triggerDatetime;
    }

    public void setTriggerDatetime(LocalDateTime triggerDatetime) {
        this.triggerDatetime = triggerDatetime;
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
        builder.append("Wal{");
        builder.append("walId = ").append(walId).append(", ");
        builder.append("hostId = ").append(hostId).append(", ");
        builder.append("triggerId = ").append(triggerId).append(", ");
        builder.append("createDatetime = ").append(createDatetime).append(", ");
        builder.append("scheduleDatetime = ").append(scheduleDatetime).append(", ");
        builder.append("triggerDatetime = ").append(triggerDatetime).append(", ");
        builder.append("state = ").append(state).append(", ");
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }
}