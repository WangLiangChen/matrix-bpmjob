package wang.liangchen.matrix.bpmjob.api;

/**
 * @author Liangchen.Wang 2023-06-27 11:54
 */
public class StackTraceElementRequest {
    /**
     * The declaring class.
     */
    private String declaringClass;
    /**
     * The method name.
     */
    private String methodName;
    /**
     * The source file name.
     */
    private String fileName;
    /**
     * The source line number.
     */
    private int lineNumber;

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
