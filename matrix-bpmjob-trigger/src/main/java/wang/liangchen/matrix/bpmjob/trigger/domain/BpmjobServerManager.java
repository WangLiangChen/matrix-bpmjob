package wang.liangchen.matrix.bpmjob.trigger.domain;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.trigger.ServerState;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.network.NetUtil;
import wang.liangchen.matrix.framework.commons.thread.ThreadUtil;
import wang.liangchen.matrix.framework.commons.uid.NumbericUid;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Liangchen.Wang
 */
//@Service
public class BpmjobServerManager implements DisposableBean {
    private final StandaloneDao standaloneDao;
    private final Long serverId;
    private final BpmjobServer heartbeatServer;
    private final short heartbeatInterval = 2;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("bpmjob-beat"));

    @Inject
    public BpmjobServerManager(StandaloneDao standaloneDao) {
        this.standaloneDao = standaloneDao;
        BpmjobServer bpmjobServer = BpmjobServer.newInstance(true);
        bpmjobServer.setServerId(NumbericUid.INSTANCE.nextId());
        bpmjobServer.setServerState(ServerState.ONLINE.getState());
        bpmjobServer.setHeartbeatInterval(heartbeatInterval);
        bpmjobServer.setServerHost(String.format("%s_%s", NetUtil.INSTANCE.getLocalHostName(), NetUtil.INSTANCE.getLocalHostAddress()));
        this.standaloneDao.insert(bpmjobServer);
        this.serverId = bpmjobServer.getServerId();
        this.heartbeatServer = BpmjobServer.newInstance();
        this.heartbeatServer.setServerId(this.serverId);
        executorService.scheduleAtFixedRate(this::heartbeat, 0, heartbeatInterval, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(this::monitor, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    private void heartbeat() {
        LocalDateTime now = DateTimeUtil.INSTANCE.ms2LocalDateTime(alignSecond());
        this.heartbeatServer.setHeartbeatDatetime(now);
        this.standaloneDao.update(heartbeatServer);
    }

    private void monitor() {
        // 查询出所有的活跃节点
        Criteria<BpmjobServer> criteria = Criteria.of(BpmjobServer.class)
                .resultFields(BpmjobServer::getServerId, BpmjobServer::getHeartbeatInterval, BpmjobServer::getHeartbeatDatetime)
                ._equals(BpmjobServer::getServerState, ServerState.ONLINE.getState());
        List<BpmjobServer> bpmjobServers = this.standaloneDao.list(criteria);
        LocalDateTime now = DateTimeUtil.INSTANCE.ms2LocalDateTime(alignSecond());

    }

    private long alignSecond() {
        long currentTimeMillis = System.currentTimeMillis();
        long offset = 1000 - currentTimeMillis % 1000;
        ThreadUtil.INSTANCE.sleep(offset);
        return currentTimeMillis + offset;
    }

    @Override
    public void destroy() {
        executorService.shutdown();
        BpmjobServer bpmjobServer = BpmjobServer.newInstance();
        bpmjobServer.setServerId(serverId);
        bpmjobServer.setOfflineDatetime(LocalDateTime.now());
        bpmjobServer.setServerState(ServerState.OFFLINE.getState());
        this.standaloneDao.update(bpmjobServer);
    }
}