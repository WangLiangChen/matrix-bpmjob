package wang.liangchen.matrix.bpmjob.domain.host;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;


/**
 * @author Liangchen.Wang
 */
@Service
public class HostManager {
    private final static Logger logger = LoggerFactory.getLogger(HostManager.class);
    private final StandaloneDao repository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("job-beat-"));

    @Inject
    public HostManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public void createHost(Host host) {
        host.setState(HostState.ONLINE.getState());
        host.initializeFields();
        this.repository.insert(host);
    }

    public void heartbeat(Long hostId, Short heartbeatInterval) {
        Host host = Host.newInstance();
        host.setHostId(hostId);
        host.setHeartbeatInterval(heartbeatInterval);
        host.setHeartbeatDatetime(LocalDateTime.now());
        host.setState(HostState.ONLINE.getState());
        this.repository.update(host);
    }

    public Set<Long> acquireOfflinedHost(LocalDateTime duration) {
        List<Host> hosts = this.repository.list(Criteria.of(Host.class)
                .resultFields(Host::getHostId)
                ._equals(Host::getState, HostState.ONLINE.getState())
                ._lessThan(Host::getHeartbeatDatetime, duration));
        return hosts.stream().map(Host::getHostId).collect(Collectors.toSet());
    }

    public int terminateHost(Long hostId,Long terminator) {
        // 终结超时没有心跳的节点，使用状态迁移：online->dead 抢占
        Host host = Host.newInstance();
        host.setTerminator(terminator);
        host.setDeadDatetime(LocalDateTime.now());
        host.setState(HostState.DEAD.getState());
        return repository.update(UpdateCriteria.of(host)
                ._equals(Host::getHostId, hostId)
                ._equals(Host::getState, HostState.ONLINE.getState())
        );
    }

    public void offline(Long hostId) {
        Host entity = Host.newInstance();
        entity.setHostId(hostId);
        entity.setState(HostState.OFFLINE.getState());
        entity.setOfflineDatetime(LocalDateTime.now());
        repository.update(entity);
    }
}