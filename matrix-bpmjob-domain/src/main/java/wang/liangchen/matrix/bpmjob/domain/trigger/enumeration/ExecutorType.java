package wang.liangchen.matrix.bpmjob.domain.trigger.enumeration;

/**
 * @author Liangchen.Wang 2022-12-11 16:21
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
