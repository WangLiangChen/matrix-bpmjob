package wang.liangchen.matrix.bpmjob.domain.host;

import jakarta.inject.Inject;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;

/**
 * @author Liangchen.Wang 2023-03-17 17:18
 */
@Service
public class HostManager {
    private final StandaloneDao repository;

    @Inject
    public HostManager(StandaloneDao repository) {
        this.repository = repository;
    }
}
