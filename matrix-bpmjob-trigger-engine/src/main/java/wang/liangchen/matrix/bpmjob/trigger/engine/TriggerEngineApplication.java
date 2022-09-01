package wang.liangchen.matrix.bpmjob.trigger.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Liangchen.Wang 2022-08-19 19:35
 */
@SpringBootApplication
public class TriggerEngineApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(TriggerEngineApplication.class);
        springApplication.run(args);
    }
}
