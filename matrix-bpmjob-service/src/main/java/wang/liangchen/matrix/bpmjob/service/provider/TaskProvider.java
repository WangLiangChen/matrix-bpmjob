package wang.liangchen.matrix.bpmjob.service.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.framework.springboot.jackson.DefaultObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Liangchen.Wang 2023-05-27 22:31
 */
@RestController
@RequestMapping("/task")
public class TaskProvider {
    @GetMapping("/getTasks")
    public List<TaskResponse> getTasks(@RequestHeader("tenant-code") String tenantCode,
                                       @RequestHeader("app-code") String appCode,
                                       int number) {
        System.out.println("get task number:" + number);
        return Collections.emptyList();
    }

    @PostMapping("/acceptTasks")
    public void acceptTasks(@RequestBody Set<Long> taskIds) throws JsonProcessingException {
        System.out.println(DefaultObjectMapper.INSTANCE.objectMapper().writeValueAsString(taskIds));
    }

    @PostMapping("/completeTask")
    public void completeTask(Long taskId) throws JsonProcessingException {

    }
}
