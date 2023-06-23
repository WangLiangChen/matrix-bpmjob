package wang.liangchen.matrix.bpmjob.api;

/**
 * @author Liangchen.Wang 2023-03-20 14:28
 */
public class TaskResponse {
    private Long taskId;
    private String className;
    private String methodName;
    private String annotationName;
    private Object jsonStringPatameter;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

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

    public Object getJsonStringPatameter() {
        return jsonStringPatameter;
    }

    public void setJsonStringPatameter(Object jsonStringPatameter) {
        this.jsonStringPatameter = jsonStringPatameter;
    }
}
