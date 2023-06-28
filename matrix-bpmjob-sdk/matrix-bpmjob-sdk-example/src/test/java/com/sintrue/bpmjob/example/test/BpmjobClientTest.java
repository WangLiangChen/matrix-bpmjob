package com.sintrue.bpmjob.example.test;

import org.junit.Test;
import wang.liangchen.matrix.bpmjob.sdk.connector.DefaultHttpConnectorFactory;
import wang.liangchen.matrix.bpmjob.sdk.connector.DefaultHttpConnectorProperties;
import wang.liangchen.matrix.bpmjob.sdk.core.BpmJobClientProperties;
import wang.liangchen.matrix.bpmjob.sdk.core.client.BpmJobClient;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.ConnectorFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.BpmjobExecutorFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.DefaultBpmjobExecutorFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author Liangchen.Wang 2023-06-28 6:58
 */
public class BpmjobClientTest {

    @Test
    public void testClient() throws InterruptedException {
        DefaultHttpConnectorProperties defaultHttpConnectorProperties = DefaultHttpConnectorProperties.getInstance();
        ConnectorFactory connectorFactory = new DefaultHttpConnectorFactory(defaultHttpConnectorProperties);
        BpmjobExecutorFactory executorFactory = new DefaultBpmjobExecutorFactory();
        BpmJobClientProperties clientProperties = BpmJobClientProperties.getInstance();
        BpmJobClient bpmJobClient = new BpmJobClient(clientProperties, connectorFactory, executorFactory);
        bpmJobClient.start();

        new CountDownLatch(1).await();
    }
}
