package wang.liangchen.matrix.bpmjob.trigger.event;

import org.springframework.context.ApplicationEvent;
import wang.liangchen.matrix.bpmjob.domain.trigger.Wal;

/**
 * @author Liangchen.Wang 2023-06-19 20:06
 */
public class TaskCreationEvent extends ApplicationEvent {
    private final String hostLabel;
    private final Wal wal;

    public TaskCreationEvent(Object source, String hostLabel, Wal wal) {
        super(source);
        this.hostLabel = hostLabel;
        this.wal = wal;
    }

    public String getHostLabel() {
        return hostLabel;
    }

    public Wal getWal() {
        return wal;
    }
}
