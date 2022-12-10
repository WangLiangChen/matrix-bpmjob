package wang.liangchen.matrix.bpmjob.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import wang.liangchen.matrix.framework.web.annotation.EnableWeb;

/**
 * @author Liangchen.Wang 2022-11-17 14:42
 */
@SpringBootApplication
@EnableWeb
public class BpmjobServiceApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(BpmjobServiceApplication.class);
        springApplication.run(args);
    }
}
