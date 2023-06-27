package wang.liangchen.matrix.bpmjob.sdk.connector.utils;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import wang.liangchen.matrix.bpmjob.sdk.core.exception.BpmJobException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2023-03-22 20:55
 */
public enum VertxWebClientUtil {
    INSTANCE;
    private final WebClient webClient;

    VertxWebClientUtil() {
        Vertx vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions();
        options.setUserAgent("bpmjob-client/2.0.0");
        options.setIdleTimeoutUnit(TimeUnit.SECONDS);
        options.setConnectTimeout(5);
        options.setReadIdleTimeout(5);
        options.setWriteIdleTimeout(5);
        options.setIdleTimeout(5);
        webClient = WebClient.create(vertx, options);
    }


    public <T> CompletionStage<List<T>> postJson(String requestURI, Map<String, String> queryParams, Object body, MultiMap headers, Credentials credentials, long timeout, Class<T> resultClass) {
        HttpRequest<Buffer> request = resolveHttpRequest(HttpMethod.POST, requestURI, queryParams, headers, credentials, timeout);
        Future<HttpResponse<Buffer>> future;
        if (null == body) {
            future = request.send();
        } else {
            future = request.sendJson(body);
        }
        return future.toCompletionStage().thenApply(response -> resolveResult(response.bodyAsJsonObject(), resultClass));
    }

    public <T> CompletionStage<T> postJson(String requestURI, Object body, Map<String, String> queryParams, MultiMap headers, Credentials credentials, long timeout, Class<T> resultClass) {
        CompletionStage<List<T>> list = postJson(requestURI, queryParams, body, headers, credentials, timeout, resultClass);
        return list.thenApply(e -> e.isEmpty() ? null : e.get(0));
    }


    public <T> CompletionStage<List<T>> getList(String requestURI, Map<String, String> queryParams, MultiMap headers, Credentials credentials, long timeout, Class<T> resultClass) {
        HttpRequest<Buffer> request = resolveHttpRequest(HttpMethod.GET, requestURI, queryParams, headers, credentials, timeout);
        return request.send().toCompletionStage().thenApply(response -> resolveResult(response.bodyAsJsonObject(), resultClass));
    }

    public <T> CompletionStage<T> getObject(String requestURI, Map<String, String> queryParams, MultiMap headers, Credentials credentials, long timeout, Class<T> resultClass) {
        CompletionStage<List<T>> list = getList(requestURI, queryParams, headers, credentials, timeout, resultClass);
        return list.thenApply(e -> e.isEmpty() ? null : e.get(0));
    }

    private HttpRequest<Buffer> resolveHttpRequest(HttpMethod httpMethod, String requestURI, Map<String, String> queryParams, MultiMap headers, Credentials credentials, long timeout) {
        HttpRequest<Buffer> request = null;
        if (HttpMethod.POST == httpMethod) {
            request = webClient.postAbs(requestURI);
        } else if (HttpMethod.GET == httpMethod) {
            request = webClient.getAbs(requestURI);
        }
        if (null == request) {
            throw new BpmJobException("Unsupported HttpMethod:" + httpMethod);
        }
        if (timeout > 0) {
            request.timeout(timeout);
        }
        if (null != credentials) {
            request.authentication(credentials);
        }
        if (null != headers) {
            request.putHeaders(headers);
        }
        if (null != queryParams) {
            queryParams.forEach(request::addQueryParam);
        }
        return request;
    }


    private <T> List<T> resolveResult(JsonObject jsonObject, Class<T> resultClass) {
        Boolean success = jsonObject.getBoolean("success", Boolean.FALSE);
        if (!success) {
            throw new BpmJobException(jsonObject.getString("message"));
        }
        if (null == resultClass || Void.class == resultClass) {
            return Collections.emptyList();
        }
        Object payload = jsonObject.getValue("payload");
        if (null == payload) {
            return Collections.emptyList();
        }
        if (payload instanceof JsonObject) {
            jsonObject = (JsonObject) payload;
            return Collections.singletonList(jsonObject.mapTo(resultClass));
        }
        if (payload instanceof JsonArray) {
            JsonArray jsonArray = (JsonArray) payload;
            if (jsonArray.isEmpty()) {
                return Collections.emptyList();
            }
            List<T> list = new ArrayList<>(jsonArray.size());
            jsonArray.forEach(e -> {
                if (e instanceof String) {
                    list.add(resultClass.cast(e));
                    return;
                }
                if (e instanceof JsonObject) {
                    list.add(((JsonObject) e).mapTo(resultClass));
                }
            });
            return list;
        }
        if (payload instanceof String) {
            return Collections.singletonList(resultClass.cast(payload));
        }
        throw new RuntimeException("The response cannot be casted to an instance of class:" + resultClass);
    }

}
