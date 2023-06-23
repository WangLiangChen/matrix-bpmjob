package wang.liangchen.matrix.bpmjob.sdk.core.exception;

/**
 * @author Liangchen.Wang 2023-05-23 9:34
 */
public class BpmJobException extends RuntimeException{
    public BpmJobException() {
    }

    public BpmJobException(String message) {
        super(message);
    }

    public BpmJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public BpmJobException(Throwable cause) {
        super(cause);
    }

    public BpmJobException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
