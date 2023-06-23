package wang.liangchen.matrix.bpmjob.console.test;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import wang.liangchen.matrix.bpmjob.service.TriggerCreateService;
import wang.liangchen.matrix.bpmjob.service.TriggerRequest;

/**
 * @author Liangchen.Wang 2022-11-08 17:33
 */
@SpringBootTest
public class TriggerTest {
    @Resource
    private TriggerCreateService triggerCreateService;

    @Test
    public void createTrigger() {
        TriggerRequest triggerRequest = new TriggerRequest();

        triggerCreateService.createTrigger(triggerRequest);
    }
}
