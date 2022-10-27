package wang.liangchen.matrix.bpmjob.trigger.domain.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.commons.datetime.DateTimeUtil;
import wang.liangchen.matrix.framework.commons.network.NetUtil;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import javax.inject.Inject;
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
@Service
@DomainService
public class RegistryManager implements DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(RegistryManager.class);
    private final StandaloneDao repository;
    private final Registry heartbeatRegistry = Registry.newInstance();
    private final Registry registry = Registry.newInstance();
    private final Short heartbeatInterval = 1;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("job-beat-"));

    @Inject
    public RegistryManager(StandaloneDao repository) {
        this.repository = repository;
        registerHost();
        startHeartbeat();
        startHostMonitor();
    }

    /**
     * register current host
     * registered as a new host when startup
     */
    private void registerHost() {
        registry.setHostType(HostType.TRIGGER.name());
        registry.setHostName(NetUtil.INSTANCE.getLocalHostName());
        registry.setHostIp(NetUtil.INSTANCE.getLocalHostAddress());
        registry.setHostPort((short) 0);
        registry.setHeartbeatInterval(heartbeatInterval);
        registry.setState(RegistryState.ONLINE.getState());
        registry.initializeFields();
        repository.insert(registry);
        heartbeatRegistry.setRegistryId(registry.getRegistryId());
        logger.info("register host:{},{}", registry.getRegistryId(), registry.getHostName());
    }

    /**
     * keepalive by heartbeat
     */
    private void startHeartbeat() {
        scheduler.scheduleWithFixedDelay(() -> {
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            heartbeatRegistry.setHeartbeatDatetime(now);
            heartbeatRegistry.setHeartbeatInterval(heartbeatInterval);
            heartbeatRegistry.setState(RegistryState.ONLINE.getState());
            repository.update(heartbeatRegistry);
            logger.debug("heartbeat:{},{}", registry.getRegistryId(), registry.getHostName());
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    /**
     * monitor other host
     * state is online
     * and The last heartbeat is less than the current time minus 2 heartbeat interval
     */
    private void startHostMonitor() {
        scheduler.scheduleWithFixedDelay(() -> {
            // 状态为"online"且两个周期没有心跳的其它节点
            LocalDateTime now = DateTimeUtil.INSTANCE.alignLocalDateTimeSecond();
            List<Registry> registries = repository.list(Criteria.of(Registry.class)
                    .resultFields(Registry::getRegistryId)
                    ._equals(Registry::getState, RegistryState.ONLINE.getState())
                    ._lessThan(Registry::getHeartbeatDatetime, now.minusSeconds(2L * heartbeatInterval)));
            Set<Long> registryIds = registries.stream().map(Registry::getRegistryId)
                    .filter(e -> !e.equals(this.registry.getRegistryId())).collect(Collectors.toSet());
            logger.debug("monitor hosts:{},{},{}", registry.getRegistryId(), registry.getHostName(), registryIds);
            terminateHosts(registryIds);
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    /**
     * Terminate Hosts
     * State transfer from online to dead
     *
     * @param registryIds Terminating hosts
     */
    private void terminateHosts(Set<Long> registryIds) {
        // 终结超时没有心跳的节点，状态迁移：online->dead
        Registry entity = Registry.newInstance();
        entity.setState(RegistryState.DEAD.getState());
        entity.setTerminator(this.registry.getRegistryId());
        for (Long registryId : registryIds) {
            entity.setDeadDatetime(LocalDateTime.now());
            int rows = repository.update(UpdateCriteria.of(entity)
                    ._equals(Registry::getRegistryId, registryId)
                    ._equals(Registry::getState, RegistryState.ONLINE.getState())
            );
            if (rows == 1) {
                logger.debug("terminate host:{},{},{}", registry.getRegistryId(), registry.getHostName(), registryId);
                //TODO 终结主机后，要承担它原有的任务
            }
        }
    }


    @Override
    public void destroy() throws Exception {
        // 停止心跳和监视
        scheduler.shutdown();
        Registry entity = Registry.newInstance();
        entity.setRegistryId(this.registry.getRegistryId());
        entity.setState(RegistryState.OFFLINE.getState());
        entity.setOfflineDatetime(LocalDateTime.now());
        repository.update(entity);
        //TODO 触发完自己的任务
    }


    public int add(Registry entity) {
        return repository.insert(entity);
    }

    public int delete(Long registryId) {
        Registry entity = Registry.newInstance();
        entity.setRegistryId(registryId);
        return repository.delete(entity);
    }

    public int update(Registry entity) {
        return repository.update(entity);
    }

    public Registry byKey(Long registryId, String... resultColumns) {
        return repository.select(Criteria.of(Registry.class)
                .resultColumns(resultColumns)
                ._equals(Registry::getRegistryId, registryId)
        );
    }

    public int stateTransition(Long registryId, Byte to, Byte... from) {
        Registry entity = Registry.newInstance();
        entity.setState(to);
        UpdateCriteria<Registry> updateCriteria = UpdateCriteria.of(entity)
                ._equals(Registry::getRegistryId, registryId)
                ._in(Registry::getState, from);
        return repository.update(updateCriteria);
    }

    public List<Registry> byStates(Byte... states) {
        return repository.list(Criteria.of(Registry.class)._in(Registry::getState, states));
    }

    public Registry getRegistry() {
        return this.registry;
    }
}