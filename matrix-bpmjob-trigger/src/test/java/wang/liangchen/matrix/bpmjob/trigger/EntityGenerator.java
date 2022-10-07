package wang.liangchen.matrix.bpmjob.trigger;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import wang.liangchen.matrix.framework.data.annotation.EnableJdbc;
import wang.liangchen.matrix.framework.generator.DomainGenerator;

import javax.inject.Inject;

/**
 * @author Liangchen.Wang 2022-10-01 11:53
 */
@SpringBootTest
@EnableJdbc
public class EntityGenerator {
    @Inject
    private DomainGenerator domainGenerator;

    @Test
    public void testGenerate() {
        domainGenerator.build();
    }
}
