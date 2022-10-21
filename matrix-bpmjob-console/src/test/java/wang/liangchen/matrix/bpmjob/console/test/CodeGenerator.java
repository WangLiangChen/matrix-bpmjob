package wang.liangchen.matrix.bpmjob.console.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import wang.liangchen.matrix.framework.data.annotation.EnableJdbc;
import wang.liangchen.matrix.framework.generator.DDDGenerator;

import javax.inject.Inject;

/**
 * @author Liangchen.Wang 2022-10-19 19:06
 */
@SpringBootTest
@EnableJdbc
public class CodeGenerator {
    @Inject
    private DDDGenerator dddGenerator;

    @Test
    public void generate() {
        dddGenerator.generate();
    }
}
