package wang.liangchen.matrix.bpmjob.sdk.core.executor;

/**
 * @author Liangchen.Wang 2023-05-24 9:08
 */
public class JavaBeanExecutor {
    private final String className;
    private final String methodName;
    private final String annotationName;

    private JavaBeanExecutor(String className, String methodName, String annotationName) {
        this.className = className;
        this.methodName = methodName;
        this.annotationName = annotationName;
    }

    public static JavaBeanExecutor newInstance(String className, String methodName, String annotationName) {
        return new JavaBeanExecutor(className, methodName, annotationName);
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

        JavaBeanExecutor that = (JavaBeanExecutor) o;

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
