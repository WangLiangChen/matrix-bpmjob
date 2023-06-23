package wang.liangchen.matrix.bpmjob.sdk.spring.executor;

import org.springframework.context.ApplicationContext;
import wang.liangchen.matrix.bpmjob.sdk.core.exception.BpmJobException;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.BpmjobExecutorFactory;

/**
 * @author Liangchen.Wang 2023-06-22 21:41
 */
public class SpringBpmjobExecutorFactory implements BpmjobExecutorFactory {
    private final ApplicationContext applicationContext;

    public SpringBpmjobExecutorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object createExecutor(String className) {
        ClassLoader classLoader = this.applicationContext.getClassLoader();
        try {
            Class<?> clazz = Class.forName(className, true, classLoader);
            return applicationContext.getBean(clazz);
        } catch (ClassNotFoundException e) {
            throw new BpmJobException(e);
        }
    }
}
