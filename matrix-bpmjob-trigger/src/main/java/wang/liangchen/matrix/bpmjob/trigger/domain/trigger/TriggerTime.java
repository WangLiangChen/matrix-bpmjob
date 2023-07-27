package wang.liangchen.matrix.bpmjob.trigger.domain.trigger;

import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2023-07-19 17:47
 */
public class TriggerTime {
    private long triggerId;
    private LocalDateTime triggerInstant;

    public void setTriggerId(long triggerId) {
        this.triggerId = triggerId;
    }

    public long getTriggerId() {
        return triggerId;
    }

    public void setTriggerInstant(LocalDateTime triggerInstant) {
        this.triggerInstant = triggerInstant;
    }

    public LocalDateTime getTriggerInstant() {
        return triggerInstant;
    }
}
