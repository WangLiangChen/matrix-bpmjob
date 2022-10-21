package wang.liangchen.matrix.bpmjob.console.domain.registry;

import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;

import javax.inject.Inject;
import java.util.List;


/**
 * @author Liangchen.Wang
 */
@Service
@DomainService
public class RegistryManager {
    private final StandaloneDao repository;

    @Inject
    public RegistryManager(StandaloneDao repository) {
        this.repository = repository;
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
                ._equals(Registry::getRegistryId, registryId))
                ;
    }

    public void stateTransition(Long registryId, Short to, Short... from) {
        Registry entity = Registry.newInstance();
        entity.setState(to);
        UpdateCriteria<Registry> updateCriteria = UpdateCriteria.of(entity)
                ._equals(Registry::getRegistryId, registryId)
                ._in(Registry::getState, from);
        repository.update(updateCriteria);
    }

    public List<Registry> byStates(Short... states) {
        return repository.list(Criteria.of(Registry.class)._in(Registry::getState, states));
    }
}