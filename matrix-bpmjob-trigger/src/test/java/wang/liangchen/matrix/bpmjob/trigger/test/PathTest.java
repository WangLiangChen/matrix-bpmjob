package wang.liangchen.matrix.bpmjob.trigger.test;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * @author Liangchen.Wang 2022-11-11 9:33
 */
public class PathTest {
    @Test
    public void testPaths() throws MalformedURLException {
        new URL("f bc");
    }

    private volatile List<String> triggerList;

    @Test
    public void doTime() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println(now);
        OffsetDateTime offsetDateTime = OffsetDateTime.now();
        System.out.println(offsetDateTime);

        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        System.out.println(zonedDateTime);
    }


}
