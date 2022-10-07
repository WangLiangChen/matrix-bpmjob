package wang.liangchen.matrix.bpmjob.trigger;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.support.CronExpression;
import wang.liangchen.matrix.bpmjob.trigger.domain.BpmjobServer;
import wang.liangchen.matrix.framework.commons.encryption.DigestSignUtil;
import wang.liangchen.matrix.framework.commons.encryption.enums.DigestAlgorithm;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.thread.ThreadUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liangchen.Wang 2022-08-19 19:19
 */
public class CommonsTest {
    @Test
    public void testServerState() {
        for (ServerState value : ServerState.values()) {
            System.out.println("oridinal:" + value.ordinal());
            System.out.println("   state:" + value.getState());
            System.out.println("    name:" + value.name());
            System.out.println("nameText:" + value.getStateText());
            System.out.println(" valueOf:" + value.valueOf(value.getState()));
        }
    }

    @Test
    public void testAlignSecond() {
        long currentTimeMillis = System.currentTimeMillis();
        long offset = 1000 - currentTimeMillis % 1000;
        System.out.println(currentTimeMillis);
        System.out.println(offset);
        System.out.println(currentTimeMillis + offset);
    }

    @Test
    public void testProtostuff() {
        BpmjobServer bpmjobServer = BpmjobServer.newInstance(true);
        List<BpmjobServer> bpmjobServers = new ArrayList<>();
        bpmjobServers.add(BpmjobServer.newInstance());
        bpmjobServers.add(BpmjobServer.newInstance(true));

        byte[] bytes = ObjectUtil.INSTANCE.protostuffSerializer(bpmjobServers);
        bpmjobServers = ObjectUtil.INSTANCE.protostuffDeserializer(bytes);
        System.out.println();
    }

}
