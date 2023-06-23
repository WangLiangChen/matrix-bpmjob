package wang.liangchen.matrix.bpmjob.sdk.spring.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import wang.liangchen.matrix.bpmjob.sdk.spring.executor.SpringBpmjobExecutorFactory;

import java.lang.reflect.Method;

/**
 * @author Liangchen.Wang 2023-06-22 22:00
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestExecutor.class, TestInnerExecutor.class})
public class ExecutorTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testExecutor() throws Exception {
        SpringBpmjobExecutorFactory factory = new SpringBpmjobExecutorFactory(applicationContext);
        Object executor = factory.createExecutor("wang.liangchen.matrix.bpmjob.sdk.spring.test.TestExecutor");
        Method method = executor.getClass().getMethod("execute", String.class);
        method.invoke(executor, "hello");
    }
}
