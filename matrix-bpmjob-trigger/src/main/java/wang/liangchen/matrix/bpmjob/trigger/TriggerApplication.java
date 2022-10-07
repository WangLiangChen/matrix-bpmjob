package wang.liangchen.matrix.bpmjob.trigger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import wang.liangchen.matrix.cache.sdk.override.EnableMatrixCaching;
import wang.liangchen.matrix.framework.data.annotation.EnableJdbc;

/**
 * @author Liangchen.Wang 2022-10-01 12:02
 */
@SpringBootApplication
@EnableJdbc
@EnableMatrixCaching
public class TriggerApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(TriggerApplication.class);
        springApplication.run(args);
    }
}
