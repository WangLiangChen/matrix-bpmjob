package liangchen.wang.matrix.bpmjob.sdk.core.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import liangchen.wang.matrix.bpmjob.sdk.core.exception.BpmJobException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Liangchen.Wang 2023-03-22 20:55
 */
public enum WebClientUtil {
    INSTANCE;
    private final WebClient webClient;

    WebClientUtil() {
        Vertx vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions();
        options.setUserAgent("bpmjob-client/2.0.0");
        options.setIdleTimeoutUnit(TimeUnit.SECONDS);
        options.setConnectTimeout(5);
        options.setReadIdleTimeout(3);
        options.setWriteIdleTimeout(3);
        options.setIdleTimeout(5);
        webClient = WebClient.create(vertx, options);
    }

    public <T> void postJson(String requestURI, Object body, long timeout, Credentials credentials, Consumer<Throwable> exceptionHandler) {
        postJson(requestURI, body, timeout, credentials, null, null, exceptionHandler);
    }

    public <T> void postJson(String requestURI, Object body, long timeout, Credentials credentials, Class<T> resultClass, Consumer<List<T>> resultHandler, Consumer<Throwable> exceptionHandler) {
        HttpRequest<Buffer> request = webClient.postAbs(requestURI);
        if (timeout > 0) {
            request.timeout(timeout);
        }
        if (null != credentials) {
            request.authentication(credentials);
        }
        if (null == body) {
            request.send(handler(resultClass, resultHandler, exceptionHandler));
            return;
        }
        request.sendJson(body, handler(resultClass, resultHandler, exceptionHandler));
    }

    private <T> Handler<AsyncResult<HttpResponse<Buffer>>> handler(Class<T> resultClass, Consumer<List<T>> resultHandler, Consumer<Throwable> exceptionHandler) {
        return ar -> {
            if (ar.failed()) {
                exceptionHandler.accept(ar.cause());
                return;
            }
            JsonObject jsonObject = ar.result().bodyAsJsonObject();
            Boolean success = jsonObject.getBoolean("success", Boolean.FALSE);
            if (!success) {
                exceptionHandler.accept(new BpmJobException(jsonObject.getString("message")));
                return;
            }

            if (null == resultClass) {
                // no callback
                return;
            }
            Object payload = jsonObject.getValue("payload");
            if (null == payload) {
                resultHandler.accept(Collections.emptyList());
                return;
            }
            if (payload instanceof JsonObject) {
                jsonObject = (JsonObject) payload;
                resultHandler.accept(Collections.singletonList(jsonObject.mapTo(resultClass)));
                return;
            }
            JsonArray jsonArray = (JsonArray) payload;
            if (jsonArray.isEmpty()) {
                resultHandler.accept(Collections.emptyList());
                return;
            }
            List<T> list = jsonArray.stream().map(e -> (JsonObject) e).map(e -> e.mapTo(resultClass)).collect(Collectors.toList());
            resultHandler.accept(list);
        };
    }

}
