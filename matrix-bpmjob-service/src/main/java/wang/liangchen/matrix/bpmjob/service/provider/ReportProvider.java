package wang.liangchen.matrix.bpmjob.service.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wang.liangchen.matrix.bpmjob.api.HeartbeatRequest;
import wang.liangchen.matrix.bpmjob.api.JavaBeanExecutorRequest;
import wang.liangchen.matrix.framework.springboot.jackson.DefaultObjectMapper;

import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-27 22:31
 */
@RestController
@RequestMapping("/report")
public class ReportProvider {
    @PostMapping("/reportMethods")
    public void reportMethods(@RequestBody Set<JavaBeanExecutorRequest> javaBeanExecutors) throws JsonProcessingException {
        System.out.println(DefaultObjectMapper.INSTANCE.objectMapper().writeValueAsString(javaBeanExecutors));
    }

    @PostMapping("/heartbeat")
    public void heartbeat(@RequestBody HeartbeatRequest heartbeatRequest) throws JsonProcessingException {
        System.out.println(DefaultObjectMapper.INSTANCE.objectMapper().writeValueAsString(heartbeatRequest));
    }
}
