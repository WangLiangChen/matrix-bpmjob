package wang.liangchen.matrix.bpmjob.sdk.spring.test;

import org.springframework.stereotype.Component;

/**
 * @author Liangchen.Wang 2023-06-22 22:06
 */
@Component
public class TestExecutor {
    private final TestInnerExecutor innerExecutor;

    public TestExecutor(TestInnerExecutor innerExecutor) {
        this.innerExecutor = innerExecutor;
    }

    public void execute(String jsonString) {
        System.out.println(jsonString + "," + innerExecutor.name());
    }
}
