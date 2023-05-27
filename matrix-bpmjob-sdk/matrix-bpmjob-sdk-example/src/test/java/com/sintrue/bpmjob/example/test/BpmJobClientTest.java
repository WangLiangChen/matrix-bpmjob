package com.sintrue.bpmjob.example.test;

import liangchen.wang.matrix.bpmjob.sdk.core.BpmJobSdkProperties;
import liangchen.wang.matrix.bpmjob.sdk.core.client.BpmJobClient;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2023-05-24 8:37
 */
public class BpmJobClientTest {
    @Test
    public void testScan() throws InterruptedException {
        BpmJobClient client = new BpmJobClient(new BpmJobSdkProperties());
        TimeUnit.SECONDS.sleep(10);

    }
}
