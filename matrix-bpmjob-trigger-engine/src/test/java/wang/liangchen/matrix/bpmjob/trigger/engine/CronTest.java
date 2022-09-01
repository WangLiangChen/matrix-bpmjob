package wang.liangchen.matrix.bpmjob.trigger.engine;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;

import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2022-08-19 19:19
 */
public class CronTest {
    @Test
    public void testCron() {
        CronExpression cronExpression = CronExpression.parse("0/5 * * * * ? ");
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 5; i++) {
            LocalDateTime next = cronExpression.next(now);
            now = next;
            System.out.println(next);
        }
    }
}
