package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnJson;
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
    private Long triggerId;
    private String walKey;
    private String hostLabel;
    /**
     * 显式创建任务的参数-动态,会覆盖合并trigger_params.如API触发、子任务、流程任务等
     */
    @ColumnJson
    private JsonField taskParams;
    /**
     * 预期触发时间
     */
    private LocalDateTime expectedDatetime;
    /**
     * 创建时间
     */
    private LocalDateTime createDatetime;

    /**
     * 0-pending;1-confirmed;
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

    public Long getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(Long triggerId) {
        this.triggerId = triggerId;
    }

    public String getWalKey() {
        return walKey;
    }

    public void setWalKey(String walKey) {
        this.walKey = walKey;
    }

    public String getHostLabel() {
        return hostLabel;
    }

    public void setHostLabel(String hostLabel) {
        this.hostLabel = hostLabel;
    }

    public JsonField getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(JsonField taskParams) {
        this.taskParams = taskParams;
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

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }
}