package wang.liangchen.bpmjob.sdk.test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2022-11-15 10:48
 */
@ExtendWith(VertxExtension.class)
public class MainTest {
    @Test
    public void testClient() throws InterruptedException {
        VertxTestContext vertxTestContext = new VertxTestContext();
        Vertx vertx = Vertx.vertx();
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setDefaultHost("www.baidu.com");
        //webClientOptions.setDefaultPort(80);
        WebClient client = WebClient.create(vertx,webClientOptions);
        System.out.println(client);
        client = WebClient.wrap((HttpClient) client);
        System.out.println(client);

        client.get("/")
                .send()
                .onSuccess(response -> {
                    System.out.println("Received response with status code" + response.bodyAsString());
                })
                .onFailure(err -> System.out.println("Something went wrong " + err.getMessage()));
        vertxTestContext.awaitCompletion(5, TimeUnit.SECONDS);
    }
}
