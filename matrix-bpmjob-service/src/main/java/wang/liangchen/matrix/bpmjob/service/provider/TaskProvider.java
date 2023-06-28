package wang.liangchen.matrix.bpmjob.service.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.bind.annotation.*;
import wang.liangchen.matrix.bpmjob.api.TaskRequest;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.framework.springboot.jackson.DefaultObjectMapper;

import java.util.ArrayList;
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
    public List<TaskResponse> getTasks(int number) {
        List<TaskResponse> tasks = new ArrayList<>();
        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setTaskId(110L);
        taskResponse.setClassName("com.sintrue.bpmjob.example.UseInterfaceImp");
        taskResponse.setMethodName("execute");
        taskResponse.setAnnotationName("execute");
        taskResponse.setJsonStringPatameter("{\"name\":\"liangchen.wang\",\"sex\":\"male\"}");
        tasks.add(taskResponse);
        return tasks;
    }

    @PostMapping("/acceptTasks")
    public void acceptTasks(@RequestBody Set<Long> taskIds) throws JsonProcessingException {
        System.out.println("acceptTask" + DefaultObjectMapper.INSTANCE.objectMapper().writeValueAsString(taskIds));
    }

    @PostMapping("/completeTask")
    public void completeTask(@RequestBody TaskRequest taskRequest) throws JsonProcessingException {
        System.out.println("completeTask:" + DefaultObjectMapper.INSTANCE.objectMapper().writeValueAsString(taskRequest));
    }
}
