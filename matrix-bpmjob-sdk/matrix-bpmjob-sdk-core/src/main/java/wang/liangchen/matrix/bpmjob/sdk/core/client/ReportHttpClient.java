package wang.liangchen.matrix.bpmjob.sdk.core.client;


import wang.liangchen.matrix.bpmjob.api.ExecutorMethod;

import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-21 15:59
 */
public class ReportHttpClient extends AbstractHttpClient {
    private final String ExecutorMethodsURI;

    public ReportHttpClient(String uri, String taskUri) {
        super(uri, taskUri);
        this.ExecutorMethodsURI = uri.concat("/report/executorMethods");
    }

    public void executorMethods(Set<ExecutorMethod> executorMethods) {
        super.postJson(this.ExecutorMethodsURI, executorMethods, 2000, ExecutorMethod.class, results -> {
            System.out.println(results);
        }, throwable -> {
            throwable.printStackTrace();
        });
    }

}
