package wang.liangchen.matrix.bpmjob.domain.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author Liangchen.Wang
 */
@Service
@DomainService
public class HostManager {
    private final static Logger logger = LoggerFactory.getLogger(HostManager.class);
    private final StandaloneDao repository;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, new CustomizableThreadFactory("job-beat-"));

    @Inject
    public HostManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public int add(Host entity) {
        return repository.insert(entity);
    }

    public int delete(Long registryId) {
        Host entity = Host.newInstance();
        entity.setHostId(registryId);
        return repository.delete(entity);
    }

    public int update(Host entity) {
        return repository.update(entity);
    }

    public Host byKey(Long registryId, String... resultColumns) {
        return repository.select(Criteria.of(Host.class)
                .resultColumns(resultColumns)
                ._equals(Host::getHostId, registryId)
        );
    }

    public int stateTransition(Long registryId, Byte to, Byte... from) {
        Host entity = Host.newInstance();
        entity.setState(to);
        UpdateCriteria<Host> updateCriteria = UpdateCriteria.of(entity)
                ._equals(Host::getHostId, registryId)
                ._in(Host::getState, from);
        return repository.update(updateCriteria);
    }

    public List<Host> byStates(Byte... states) {
        return repository.list(Criteria.of(Host.class)._in(Host::getState, states));
    }

}