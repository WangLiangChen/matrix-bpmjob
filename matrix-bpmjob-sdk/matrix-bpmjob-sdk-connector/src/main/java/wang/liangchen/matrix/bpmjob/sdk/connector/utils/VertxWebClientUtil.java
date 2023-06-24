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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public <T> CompletionStage<List<T>> postJson(String requestURI, Object body, long timeout, Credentials credentials, MultiMap headers, MultiMap queryParams, Class<T> resultClass) {
        HttpRequest<Buffer> request = resolveHttpRequest(HttpMethod.POST, requestURI, timeout, credentials, headers, queryParams);
        Future<HttpResponse<Buffer>> future;
        if (null == body) {
            future = request.send();
        } else {
            future = request.sendJson(body);
        }
        return future.toCompletionStage().thenApply(response -> resolveResult(response.bodyAsJsonObject(), resultClass));
    }

    public <T> CompletionStage<List<T>> getJson(String requestURI, long timeout, Credentials credentials, MultiMap headers, MultiMap queryParams, Class<T> resultClass) {
        HttpRequest<Buffer> request = resolveHttpRequest(HttpMethod.GET, requestURI, timeout, credentials, headers, queryParams);
        return request.send().toCompletionStage().thenApply(response -> resolveResult(response.bodyAsJsonObject(), resultClass));
    }

    private HttpRequest<Buffer> resolveHttpRequest(HttpMethod httpMethod, String requestURI, long timeout, Credentials credentials, MultiMap headers, MultiMap queryParams) {
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
            return jsonArray.stream().map(e -> (JsonObject) e).map(e -> e.mapTo(resultClass)).collect(Collectors.toList());
        }
        return Collections.singletonList(resultClass.cast(payload));
    }

}
