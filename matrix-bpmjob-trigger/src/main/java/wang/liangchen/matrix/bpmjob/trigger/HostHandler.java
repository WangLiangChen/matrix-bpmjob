package wang.liangchen.matrix.bpmjob.trigger;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.host.Host;
import wang.liangchen.matrix.bpmjob.domain.host.HostManager;
import wang.liangchen.matrix.bpmjob.domain.host.HostType;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.network.NetUtil;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @author Liangchen.Wang
 */
@Service
public class HostHandler implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(HostHandler.class);
    private final HostManager hostManager;
    private final Host host = Host.newInstance();
    private final Short heartbeatInterval = 2;

    private final ScheduledExecutorService hostPool = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("job-beat-"));

    @Inject
    public HostHandler(HostManager hostManager) {
        this.hostManager = hostManager;
        registerHost();

        startHeartbeat();
        startHostMonitor();
    }

    /**
     * register current host
     * registered as a new host when startup
     */
    private void registerHost() {
        host.setHostType(HostType.TRIGGER.name());
        host.setHostLabel(NetUtil.INSTANCE.getLocalHostName());
        host.setHostIp(NetUtil.INSTANCE.getLocalHostAddress());
        host.setHostPort((short) 0);
        host.setHeartbeatInterval(heartbeatInterval);
        hostManager.createHost(host);
        logger.info("register host:{},{}", host.getHostId(), host.getHostLabel());
    }

    /**
     * keepalive by heartbeat
     */
    private void startHeartbeat() {
        hostPool.scheduleWithFixedDelay(() -> {
            this.hostManager.heartbeat(this.host.getHostId(), heartbeatInterval);
            logger.debug("heartbeat:{},{}", host.getHostId(), host.getHostLabel());
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    /**
     * monitor other host
     * state is online
     * and The last heartbeat is less than the current time minus 2 heartbeat interval
     */
    private void startHostMonitor() {
        hostPool.scheduleWithFixedDelay(() -> {
            // 终止 状态为"online"且两个周期没有心跳的其它节点
            // 1、获取所有状态为online的数据，java判断周期差
            // 2、数据库判断状态为online和周期差
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            // past: heartbeat < now - 3 * heartbeatInterval
            Set<Long> hostIds = hostManager.acquireOfflinedHost(now.minusSeconds(3L * heartbeatInterval));
            logger.debug("offline hosts:{},{},{}", host.getHostId(), host.getHostLabel(), hostIds);
            terminateHosts(hostIds);
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    /**
     * Terminate Hosts
     * State transfer from online to dead
     *
     * @param hostIds Terminating hosts
     */
    private void terminateHosts(Set<Long> hostIds) {
        for (Long hostId : hostIds) {
            int rows = hostManager.terminateHost(hostId, this.getHost().getHostId());
            if (rows == 1) {
                logger.info("terminate host:{},{},{}", host.getHostId(), host.getHostLabel(), hostId);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        // 停止心跳和监视
        logger.info("heartbeat shutdown ...");
        hostPool.shutdown();
        hostPool.awaitTermination(1, TimeUnit.MINUTES);
        hostManager.offline(this.host.getHostId());
    }

    public Host getHost() {
        return this.host;
    }
}