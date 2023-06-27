package com.sintrue.bpmjob.example;

import wang.liangchen.matrix.bpmjob.sdk.core.annotation.BpmJobExecutor;

/**
 * @author Liangchen.Wang 2023-05-24 8:26
 */
public class UseAnnotation {

    @BpmJobExecutor
    public void methodA(String jsonString){

    }
    @BpmJobExecutor("methodBAlias")
    public void methodB(String jsonString){

    }
}
