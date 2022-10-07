package wang.liangchen.matrix.bpmjob.trigger.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import wang.liangchen.matrix.bpmjob.trigger.ServerState;
import wang.liangchen.matrix.bpmjob.trigger.domain.BpmjobServer;
import wang.liangchen.matrix.bpmjob.trigger.domain.BpmjobServerManager;
import wang.liangchen.matrix.cache.sdk.cache.CacheManager;
import wang.liangchen.matrix.cache.sdk.override.EnableMatrixCaching;
import wang.liangchen.matrix.framework.commons.thread.ThreadUtil;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;

import javax.inject.Inject;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang 2022-10-01 12:27
 */
@SpringBootTest
public class ServerTest {

    @Inject
    private StandaloneDao standaloneDao;
    @Inject
    private CacheManager cacheManager;

    @Test
    public void testList() {
        Criteria<BpmjobServer> criteria = Criteria.of(BpmjobServer.class).resultFields(BpmjobServer::getServerId, BpmjobServer::getHeartbeatDatetime)
                ._equals(BpmjobServer::getServerState, ServerState.OFFLINE.getState());
        List<BpmjobServer> list=standaloneDao.list(criteria);
        System.out.println(list);
        list=standaloneDao.list(criteria);
        System.out.println(list);
    }
    @Test
    public void testCache(){
        Cache test = cacheManager.getCache("test", Duration.ofMinutes(1));
        test.get("wanglc",()-> "wangliangchen");
        test.get("wanglc",()-> "wangliangchen");
        test.get("wanglc",()-> "wangliangchen");
    }
}
