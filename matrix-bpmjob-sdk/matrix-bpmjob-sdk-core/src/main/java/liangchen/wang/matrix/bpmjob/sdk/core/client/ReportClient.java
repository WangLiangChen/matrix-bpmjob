package liangchen.wang.matrix.bpmjob.sdk.core.client;

import liangchen.wang.matrix.bpmjob.sdk.core.BpmJobSdkProperties;
import wang.liangchen.matrix.bpmjob.api.ExecutorMethod;

import java.util.Set;


/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public class ReportClient {
    private final BpmJobSdkProperties bpmJobSdkProperties;

    public ReportClient(BpmJobSdkProperties bpmJobSdkProperties) {
        this.bpmJobSdkProperties = bpmJobSdkProperties;
    }

    public void reportExecutorMethods(Set<ExecutorMethod> bpmJobExecutorMethods){

    }

}
