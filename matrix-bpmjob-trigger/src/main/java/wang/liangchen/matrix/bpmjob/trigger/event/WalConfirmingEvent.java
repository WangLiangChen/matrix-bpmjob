package wang.liangchen.matrix.bpmjob.trigger.event;

import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Wal;

/**
 * @author Liangchen.Wang 2023-07-27 21:58
 */
public class WalConfirmingEvent extends BpmjobEvent {
    private final Wal wal;
    private final Trigger trigger;

    public WalConfirmingEvent(Object source, Wal wal, Trigger trigger) {
        super(source);
        this.wal = wal;
        this.trigger = trigger;
    }

    public Wal getWal() {
        return wal;
    }

    public Trigger getTrigger() {
        return trigger;
    }
}
