package wang.liangchen.matrix.bpmjob.api;

/**
 * @author Liangchen.Wang 2023-05-24 9:08
 */
public class ExecutorMethod {
    private String className;
    private String methodName;
    private String annotationName;
    public ExecutorMethod(){}

    public ExecutorMethod(String className, String methodName, String annotationName) {
        this.className = className;
        this.methodName = methodName;
        this.annotationName = annotationName;
    }

    public static ExecutorMethod newInstance(String className, String methodName, String annotationName) {
        return new ExecutorMethod(className, methodName, annotationName);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setAnnotationName(String annotationName) {
        this.annotationName = annotationName;
    }

    public String getClassName() {
        return className;
    }


    public String getMethodName() {
        return methodName;
    }


    public String getAnnotationName() {
        return annotationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutorMethod that = (ExecutorMethod) o;

        if (!className.equals(that.className)) return false;
        if (!methodName.equals(that.methodName)) return false;
        return annotationName.equals(that.annotationName);
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + methodName.hashCode();
        result = 31 * result + annotationName.hashCode();
        return result;
    }
}
