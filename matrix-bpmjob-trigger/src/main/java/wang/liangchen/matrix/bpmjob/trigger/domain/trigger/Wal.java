package wang.liangchen.matrix.bpmjob.trigger.domain.trigger;

import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/**
 * Write-Ahead Logging
 *
 * @author Liangchen.Wang 2022-10-26 09:04:57
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
    @Column(name = "trigger_id")
    private Long triggerId;
    /**
     * PrimaryKey
     */
    @Column(name = "registry_id")
    private Long registryId;
    /**
     * 创建时间
     */
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

    public Long getTriggerId() {
        return this.triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public Long getRegistryId() {
        return this.registryId;
    }

    public void setRegistryId(Long registryId) {
        this.registryId = registryId;
    }

    public LocalDateTime getScheduleDatetime() {
        return this.scheduleDatetime;
    }

    public void setCreateDatetime(LocalDateTime createDatetime) {
        this.createDatetime = createDatetime;
    }

    public LocalDateTime getCreateDatetime() {
        return createDatetime;
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
        builder.append("triggerId = ").append(triggerId).append(", ");
        builder.append("registryId = ").append(registryId).append(", ");
        builder.append("scheduleDatetime = ").append(scheduleDatetime).append(", ");
        builder.append("triggerDatetime = ").append(triggerDatetime).append(", ");
        builder.append("state = ").append(state).append(", ");
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }


}