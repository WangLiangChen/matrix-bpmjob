package wang.liangchen.matrix.bpmjob.domain.executor;

import jakarta.persistence.Entity;
import wang.liangchen.matrix.bpmjob.domain.Isolation;

/**
 * @author Liangchen.Wang 2023-06-24 14:08
 */
@Entity(name = "bpmjob_javabean_executor")
public class JavaBeanExecutor extends Isolation {
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
