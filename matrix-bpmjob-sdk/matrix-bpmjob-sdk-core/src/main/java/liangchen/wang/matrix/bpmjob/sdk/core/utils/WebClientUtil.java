package liangchen.wang.matrix.bpmjob.sdk.core.utils;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2023-03-22 20:55
 */
public enum WebClientUtil {
    INSTANCE;
    private final WebClient webClient;

    WebClientUtil() {
        Vertx vertx = Vertx.vertx();
        WebClientOptions options = new WebClientOptions();
        options.setIdleTimeoutUnit(TimeUnit.SECONDS);
        options.setConnectTimeout(5);
        options.setReadIdleTimeout(3);
        options.setWriteIdleTimeout(1);
        options.setIdleTimeout(1);
        webClient = WebClient.create(vertx, options);
    }

}
