package com.sintrue.bpmjob.example.test;

import liangchen.wang.matrix.bpmjob.sdk.core.BpmJobSdkProperties;
import liangchen.wang.matrix.bpmjob.sdk.core.client.BpmJobClient;
import liangchen.wang.matrix.bpmjob.sdk.core.thread.BpmJobExecutor;
import org.junit.Test;

/**
 * @author Liangchen.Wang 2023-05-24 8:37
 */
public class BpmJobClientTest {
    @Test
    public void testScan() {
        BpmJobClient client = new BpmJobClient(new BpmJobSdkProperties());

    }
}
