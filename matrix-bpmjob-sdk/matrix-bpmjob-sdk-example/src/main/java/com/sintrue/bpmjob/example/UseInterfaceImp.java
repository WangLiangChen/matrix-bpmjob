package com.sintrue.bpmjob.example;

import wang.liangchen.matrix.bpmjob.sdk.core.executor.IBpmJobExecutor;

/**
 * @author Liangchen.Wang 2023-05-24 8:35
 */
public class UseInterfaceImp implements IBpmJobExecutor {
    @Override
    public void execute(String jsonString) {
        System.out.println("execute task:" + jsonString+(1/0));
    }
}
