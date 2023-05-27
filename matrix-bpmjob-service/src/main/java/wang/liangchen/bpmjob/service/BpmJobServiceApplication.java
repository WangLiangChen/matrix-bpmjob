package wang.liangchen.bpmjob.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import wang.liangchen.matrix.framework.web.annotation.EnableWeb;

/**
 * @author Liangchen.Wang 2023-05-27 22:38
 */
@SpringBootApplication
@EnableWeb
public class BpmJobServiceApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(BpmJobServiceApplication.class);
        springApplication.run(args);
    }
}
