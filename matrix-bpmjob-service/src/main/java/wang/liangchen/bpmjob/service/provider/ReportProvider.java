package wang.liangchen.bpmjob.service.provider;

import org.springframework.web.bind.annotation.*;
import wang.liangchen.matrix.bpmjob.api.JavaBeanExecutorRequest;

import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-27 22:31
 */
@RestController
@RequestMapping("/report")
public class ReportProvider {
    @PostMapping("/javaBeanExecutors")
    public void javaBeanExecutors(@RequestHeader("tenant-code") String tenantCode,
                                  @RequestHeader("app-code") String appCode,
                                  @RequestBody Set<JavaBeanExecutorRequest> javaBeanExecutors) {

    }
}
