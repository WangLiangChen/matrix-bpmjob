package wang.liangchen.matrix.bpmjob.sdk.core.executor;

import wang.liangchen.matrix.bpmjob.sdk.core.exception.BpmJobException;

/**
 * @author Liangchen.Wang 2023-06-23 9:12
 */
public class DefaultBpmjobExecutorFactory implements BpmjobExecutorFactory {
    @Override
    public Object createExecutor(String className) {
        try {
            return Class.forName(className).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BpmJobException(e);
        }
    }
}
