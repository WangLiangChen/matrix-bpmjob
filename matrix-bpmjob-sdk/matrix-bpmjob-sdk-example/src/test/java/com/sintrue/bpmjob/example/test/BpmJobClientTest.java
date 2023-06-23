package com.sintrue.bpmjob.example.test;

import org.junit.Test;
import wang.liangchen.matrix.bpmjob.sdk.core.BpmJobSdkProperties;
import wang.liangchen.matrix.bpmjob.sdk.core.client.BpmJobClient;

import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2023-05-24 8:37
 */
public class BpmJobClientTest {
    @Test
    public void testScan() throws InterruptedException {
        BpmJobClient client = new BpmJobClient(BpmJobSdkProperties.getInstance(), taskClientFactory, executorFactory);
        TimeUnit.SECONDS.sleep(6);

    }
}
