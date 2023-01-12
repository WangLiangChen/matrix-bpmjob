package wang.liangchen.matrix.bpmjob.console.test;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.support.CronExpression;
import wang.liangchen.matrix.bpmjob.domain.trigger.Trigger;
import wang.liangchen.matrix.bpmjob.domain.trigger.TriggerManager;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.MissStrategy;
import wang.liangchen.matrix.bpmjob.domain.trigger.enumeration.TriggerState;

import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang 2022-11-08 17:33
 */
@SpringBootTest
public class TriggerTest {
    @Resource
    private TriggerManager manager;

    @Test
    public void add() {
        Trigger trigger = Trigger.newInstance();
        trigger.setTriggerGroup("wanglc");
        trigger.setTriggerName("TestTriggerCron");
        trigger.setTriggerExpression("0/3 * * * * ? ");
        CronExpression cronExpression = CronExpression.parse(trigger.getTriggerExpression());
        trigger.setTriggerNext(cronExpression.next(LocalDateTime.now()));
        trigger.setMissThreshold((byte) 5);
        trigger.setMissStrategy(MissStrategy.COMPENSATE);
        trigger.setState(TriggerState.NORMAL);
        trigger.initializeFields();
        manager.add(trigger);
    }
}
