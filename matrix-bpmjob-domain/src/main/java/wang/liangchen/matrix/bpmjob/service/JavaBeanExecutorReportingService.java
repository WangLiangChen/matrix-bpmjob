package wang.liangchen.matrix.bpmjob.service;

import jakarta.inject.Inject;
import org.springframework.stereotype.Component;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;

/**
 * @author Liangchen.Wang 2023-06-24 14:46
 */
@Component
public class JavaBeanExecutorReportingService {
    private final StandaloneDao standaloneDao;

    @Inject
    public JavaBeanExecutorReportingService(StandaloneDao standaloneDao) {
        this.standaloneDao = standaloneDao;
    }
    public void report(){

    }
}
