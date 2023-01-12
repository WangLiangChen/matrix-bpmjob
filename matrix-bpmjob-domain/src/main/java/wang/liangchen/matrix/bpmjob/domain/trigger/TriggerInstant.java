package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import java.time.LocalDateTime;

/**
 * 触发器触发时刻
 *
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@Entity(name = "bpmjob_trigger_instant")
public class TriggerInstant extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    private Long triggerId;
    /**
     * 下次预期触发时间
     */
    private LocalDateTime triggerInstant;


    public static TriggerInstant newInstance() {
        return ClassUtil.INSTANCE.instantiate(TriggerInstant.class);
    }

    public static TriggerInstant newInstance(boolean initializeFields) {
        TriggerInstant entity = ClassUtil.INSTANCE.instantiate(TriggerInstant.class);
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

    public LocalDateTime getTriggerInstant() {
        return triggerInstant;
    }

    public void setTriggerInstant(LocalDateTime triggerInstant) {
        this.triggerInstant = triggerInstant;
    }
}