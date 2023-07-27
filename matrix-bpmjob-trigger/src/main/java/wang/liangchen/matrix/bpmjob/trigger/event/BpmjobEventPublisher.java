package wang.liangchen.matrix.bpmjob.trigger.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liangchen.Wang 2023-07-27 16:58
 */
public enum BpmjobEventPublisher {
    INSTANCE;
    private final List<BpmjobEventListener<BpmjobEvent>> listeners = new ArrayList<>();

    public void registerObserver(BpmjobEventListener<BpmjobEvent> eventListener) {
        listeners.add(eventListener);
    }

    public void removeObserver(BpmjobEventListener<BpmjobEvent> eventListener) {
        listeners.remove(eventListener);
    }

    public void publishEvent(BpmjobEvent event) {
        for (BpmjobEventListener<BpmjobEvent> listener : listeners) {
            listener.onEvent(event);
        }
    }


}
