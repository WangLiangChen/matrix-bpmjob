package wang.liangchen.matrix.bpmjob.trigger.event;

import java.util.EventListener;

/**
 * @author Liangchen.Wang 2023-07-27 17:10
 */
public interface BpmjobEventListener<T extends BpmjobEvent> extends EventListener {
    void onEvent(T event);
}
