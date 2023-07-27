package wang.liangchen.matrix.bpmjob.trigger.exception;

/**
 * @author Liangchen.Wang 2023-07-13 15:57
 */
public class TriggerRuntimeException extends RuntimeException {
    public TriggerRuntimeException() {
    }

    public TriggerRuntimeException(String message) {
        super(message);
    }

    public TriggerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TriggerRuntimeException(Throwable cause) {
        super(cause);
    }

    public TriggerRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
