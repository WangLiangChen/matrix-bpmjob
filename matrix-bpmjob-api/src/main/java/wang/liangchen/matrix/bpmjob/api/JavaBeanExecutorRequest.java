package wang.liangchen.matrix.bpmjob.api;

/**
 * @author Liangchen.Wang 2023-05-24 9:08
 */
public class JavaBeanExecutorRequest {
    private String className;
    private String methodName;
    private String annotationName;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public void setAnnotationName(String annotationName) {
        this.annotationName = annotationName;
    }
}
