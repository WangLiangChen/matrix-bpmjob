package wang.liangchen.matrix.bpmjob.domain.trigger.enumeration;

/**
 * @author Liangchen.Wang 2023-01-12 17:20
 */
public enum ExecutorType {
    /**
     * CLASS
     * METHOD
     */
    JAVA_EXECUTOR,
    /**
     * Java(groovy)
     * shell
     * python
     * php
     * nodejs
     * powershell
     */
    SCRIPT_EXECUTOR
}
