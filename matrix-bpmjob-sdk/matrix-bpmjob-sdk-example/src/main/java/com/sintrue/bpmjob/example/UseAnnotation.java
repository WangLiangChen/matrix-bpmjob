package com.sintrue.bpmjob.example;

import wang.liangchen.matrix.bpmjob.sdk.core.annotation.BpmJob;

/**
 * @author Liangchen.Wang 2023-05-24 8:26
 */
public class UseAnnotation {

    @BpmJob
    public void methodA(String jsonString){

    }
    @BpmJob("methodBAlias")
    public void methodB(String jsonString){

    }
}
