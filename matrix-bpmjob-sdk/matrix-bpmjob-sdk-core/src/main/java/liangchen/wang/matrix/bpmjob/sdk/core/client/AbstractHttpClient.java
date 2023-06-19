package liangchen.wang.matrix.bpmjob.sdk.core.client;

import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.TokenCredentials;
import liangchen.wang.matrix.bpmjob.sdk.core.utils.WebClientUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Liangchen.Wang 2023-05-28 10:32
 */
public abstract class AbstractHttpClient {
    private final String uri;
    private final String taskUri;

    protected AbstractHttpClient(String uri, String taskUri) {
        this.uri = uri;
        this.taskUri = taskUri;
    }

    public String getUri() {
        return uri;
    }

    public String getTaskUri() {
        return taskUri;
    }

    protected <T> void postJson(String requestUri, Object body, long timeout, Class<T> resultClass, Consumer<List<T>> resultHandler, Consumer<Throwable> exceptionHandler) {
        Credentials credentials = new TokenCredentials("this is a token");
        WebClientUtil.INSTANCE.postJson(requestUri, body, timeout, credentials, resultClass, resultHandler, exceptionHandler);
    }
}
