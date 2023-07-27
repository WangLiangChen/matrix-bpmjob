package wang.liangchen.matrix.bpmjob.trigger.event;

import java.util.EventObject;

/**
 * @author Liangchen.Wang 2023-07-27 16:58
 */
public abstract class BpmjobEvent extends EventObject {
    private final long timestamp;
    public BpmjobEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
