package wang.liangchen.matrix.bpmjob.api;


import java.io.Serializable;
import java.util.Locale;

/**
 * @author Liangchen.Wang
 */
public final class FormattedResponse<T> implements Serializable {
    /**
     * 业务成功失败标识
     */
    private boolean success;
    /**
     * 提示类型/级别
     */
    private String level;
    /**
     * 业务/错误代码
     */
    private String code;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 提示信息国际化Key
     */
    private String i18n;
    /**
     * 语言
     */
    private Locale locale;
    /**
     * 业务/错误数据
     */
    private T payload;
    /**
     * 前端传递的requestId,原样返回
     * 用于标识同一个请求,或作为traceId向后传递
     */
    private String requestId;
    /**
     * 用于调试的异常堆栈信息
     */
    private String debug;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getI18n() {
        return i18n;
    }

    public void setI18n(String i18n) {
        this.i18n = i18n;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }
}
