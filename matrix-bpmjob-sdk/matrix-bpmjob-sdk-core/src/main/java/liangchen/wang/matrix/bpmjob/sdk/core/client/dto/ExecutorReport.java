package liangchen.wang.matrix.bpmjob.sdk.core.client.dto;

/**
 * @author Liangchen.Wang 2023-05-24 9:08
 */
public class ExecutorReport {
    private final String className;
    private final String methodName;
    private final String annotationName;

    public ExecutorReport(String className, String methodName, String annotationName) {
        this.className = className;
        this.methodName = methodName;
        this.annotationName = annotationName;
    }

    public String getClassName() {
        return className;
    }


    public String getMethodName() {
        return methodName;
    }


    public String getAnnotationName() {
        if (null == annotationName) {
            return methodName;
        }
        return annotationName;
    }

}
