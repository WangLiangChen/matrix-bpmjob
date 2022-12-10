package wang.liangchen.matrix.bpmjob.domain.trigger;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.ddd.domain.DomainService;


/**
 * @author Liangchen.Wang
 */
@Service
@DomainService
public class TriggerManager {
    private final static Logger logger = LoggerFactory.getLogger(TriggerManager.class);
    private final StandaloneDao repository;

    @Inject
    public TriggerManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public void add(Trigger entity) {
        repository.insert(entity);
    }
}