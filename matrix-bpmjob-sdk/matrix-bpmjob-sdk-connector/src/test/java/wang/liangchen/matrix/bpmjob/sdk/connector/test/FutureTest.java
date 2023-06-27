package wang.liangchen.matrix.bpmjob.sdk.connector.test;

import org.junit.Test;
import wang.liangchen.matrix.bpmjob.sdk.connector.utils.VertxWebClientUtil;

import java.util.List;
import java.util.concurrent.CompletionStage;


/**
 * @author Liangchen.Wang 2023-06-24 7:17
 */
public class FutureTest {
    @Test
    public void testCompletionStage() throws InterruptedException {
        CompletionStage<List<String>> list = VertxWebClientUtil.INSTANCE.getList("http://127.0.0.1:8080/example/getList1", null, null, null, 0, String.class);
    }
}
