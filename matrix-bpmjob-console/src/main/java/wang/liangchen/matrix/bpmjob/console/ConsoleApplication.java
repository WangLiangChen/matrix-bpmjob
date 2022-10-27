package wang.liangchen.matrix.bpmjob.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import wang.liangchen.matrix.framework.data.annotation.EnableJdbc;
import wang.liangchen.matrix.framework.web.annotation.EnableWeb;

/**
 * @author Liangchen.Wang 2022-10-01 12:02
 */
@SpringBootApplication
@EnableJdbc
@EnableWeb
public class ConsoleApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(ConsoleApplication.class);
        springApplication.run(args);
    }
}
