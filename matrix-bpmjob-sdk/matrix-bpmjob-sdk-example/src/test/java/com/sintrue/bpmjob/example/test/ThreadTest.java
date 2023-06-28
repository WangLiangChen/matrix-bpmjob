package com.sintrue.bpmjob.example.test;

import org.junit.Test;
import wang.liangchen.matrix.bpmjob.sdk.core.thread.BpmJobThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2023-06-28 9:30
 */
public class ThreadTest {
    @Test
    public void testException() throws InterruptedException {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new BpmJobThreadFactory("pool", "group"));
        scheduledExecutorService.scheduleWithFixedDelay(new TestRunnable(), 0, 2, TimeUnit.SECONDS);
        TimeUnit.SECONDS.sleep(10);
    }

    private static class TestRunnable implements Runnable {
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + "==-=");
            throw new RuntimeException("hello");
        }
    }
}
