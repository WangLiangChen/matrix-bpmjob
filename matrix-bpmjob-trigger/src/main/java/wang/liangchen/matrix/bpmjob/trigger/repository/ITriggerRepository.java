package wang.liangchen.matrix.bpmjob.trigger.repository;


import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.TriggerTime;
import wang.liangchen.matrix.bpmjob.trigger.domain.trigger.Wal;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Liangchen.Wang 2023-07-12 15:44
 */
public interface ITriggerRepository {
    List<TriggerTime> loadWaitingTriggers(Duration duration, int limit);

    boolean compareAndSwapTriggerTime(Long triggerId, LocalDateTime oldValue, LocalDateTime newValue);

    Trigger loadWaitingTrigger(Long triggerId);

    Trigger loadTriggerForTask(Long triggerId);

    Optional<Wal> createWal(String hostLabel, Long triggerId, LocalDateTime expectedDatetime, LocalDateTime nextDatetime);

    List<Long> loadWaitingWals(Duration duration, int limit);

    Wal loadWal(Long walId);

    boolean confirmWal(Long walId);

    void confirmWal(Wal wal, Consumer<Boolean> consumer);
}
