package com.sintrue.bpmjob.example.test;

import org.junit.Test;

import javax.annotation.Resource;

/**
 * @author Liangchen.Wang 2023-05-24 8:37
 */
public class IBpmJobExecutorClientTest {
    @Resource
    private ProxyObject proxyObject;

    @Test
    public void testProxy() throws InterruptedException {
        proxyObject.proxyedMethod();
    }
}
