package wang.liangchen.matrix.bpmjob.common.utils;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.cronutils.model.CronType.QUARTZ;

/**
 * @author Liangchen.Wang 2023-07-28 16:24
 */
public enum ThreadUtil {
    INSTANCE;

    public void sleep(TimeUnit timeUnit, long timeout) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public void sleep(long timeoutMS) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeoutMS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
