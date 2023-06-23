package wang.liangchen.matrix.bpmjob.sdk.core.executor;

/**
 * @author Liangchen.Wang 2023-06-22 21:34
 */
public interface BpmjobExecutorFactory {
    Object createExecutor(String className);
}
