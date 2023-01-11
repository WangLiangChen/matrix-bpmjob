package wang.liangchen.matrix.bpmjob.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.host.Host;
import wang.liangchen.matrix.bpmjob.domain.host.HostState;
import wang.liangchen.matrix.bpmjob.domain.host.HostType;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.network.NetUtil;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author Liangchen.Wang
 */
@Service("Trigger_HostManager")
@DomainService
public class HostManager implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(HostManager.class);
    private final StandaloneDao repository;
    private final Host heartbeatHost = Host.newInstance();
    private final Host host = Host.newInstance();
    private final Host deadHost = Host.newInstance();
    private final Short heartbeatInterval = 2;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("job-beat-"));

    @Inject
    public HostManager(StandaloneDao repository) {
        this.repository = repository;
        registerHost();
        // 设置心跳默认数据
        heartbeatHost.setHostId(this.host.getHostId());
        heartbeatHost.setState(HostState.ONLINE.getState());
        // 设置僵死状态默认数据
        deadHost.setState(HostState.DEAD.getState());

        // startHeartbeat();
        // startHostMonitor();
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
        host.setState(HostState.ONLINE.getState());
        host.initializeFields();
        repository.insert(host);
        logger.info("register host:{},{}", host.getHostId(), host.getHostLabel());
    }

    /**
     * keepalive by heartbeat
     */
    private void startHeartbeat() {
        scheduler.scheduleWithFixedDelay(() -> {
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            heartbeatHost.setHeartbeatDatetime(now);
            heartbeatHost.setHeartbeatInterval(heartbeatInterval);
            repository.update(heartbeatHost);
            logger.debug("heartbeat:{},{}", host.getHostId(), host.getHostLabel());
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    /**
     * monitor other host
     * state is online
     * and The last heartbeat is less than the current time minus 2 heartbeat interval
     */
    private void startHostMonitor() {
        scheduler.scheduleWithFixedDelay(() -> {
            // 终止 状态为"online"且两个周期没有心跳的其它节点
            // 1、获取所有状态为online的数据，java判断周期差
            // 2、数据库判断状态为online和周期差
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            List<Host> hosts = repository.list(Criteria.of(Host.class)
                    .resultFields(Host::getHostId)
                    ._equals(Host::getState, HostState.ONLINE.getState())
                    ._lessThan(Host::getHeartbeatDatetime, now.minusSeconds(2L * heartbeatInterval)));
            Set<Long> hostIds = hosts.stream().map(Host::getHostId)
                    .filter(e -> !e.equals(this.host.getHostId())).collect(Collectors.toSet());
            logger.debug("monitor hosts:{},{},{}", host.getHostId(), host.getHostLabel(), hostIds);
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
        // 终结超时没有心跳的节点，使用状态迁移：online->dead 抢占
        deadHost.setTerminator(this.host.getHostId());
        for (Long hostId : hostIds) {
            deadHost.setDeadDatetime(LocalDateTime.now());
            int rows = repository.update(UpdateCriteria.of(deadHost)
                    ._equals(Host::getHostId, hostId)
                    ._equals(Host::getState, HostState.ONLINE.getState())
            );
            if (rows == 1) {
                logger.info("terminate host:{},{},{}", host.getHostId(), host.getHostLabel(), hostId);
                //TODO 终结主机后，要承担它原有的任务
            }
        }
    }


    @Override
    public void destroy() throws Exception {
        // 停止心跳和监视
        scheduler.shutdown();
        Host entity = Host.newInstance();
        entity.setHostId(this.host.getHostId());
        entity.setState(HostState.OFFLINE.getState());
        entity.setOfflineDatetime(LocalDateTime.now());
        repository.update(entity);
        //TODO 触发完自己的任务
    }


    public int add(Host entity) {
        return repository.insert(entity);
    }

    public Host getHost() {
        return this.host;
    }
}