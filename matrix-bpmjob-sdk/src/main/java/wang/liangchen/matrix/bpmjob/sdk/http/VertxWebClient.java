package wang.liangchen.matrix.bpmjob.sdk.http;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import wang.liangchen.matrix.bpmjob.sdk.context.SdkProperties;

/**
 * @author Liangchen.Wang 2022-11-16 14:10
 */
public class VertxWebClient implements IWebClient {
    private final WebClient delegate;

    public VertxWebClient(SdkProperties sdkProperties) {
        Vertx vertx = Vertx.vertx();
        WebClientOptions webClientOptions = new WebClientOptions();
        this.delegate = WebClient.create(vertx, webClientOptions);
    }
}
