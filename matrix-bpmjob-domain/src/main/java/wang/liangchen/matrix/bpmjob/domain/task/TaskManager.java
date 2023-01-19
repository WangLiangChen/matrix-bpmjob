package wang.liangchen.matrix.bpmjob.domain.task;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.host.Host;
import wang.liangchen.matrix.bpmjob.domain.trigger.Wal;
import wang.liangchen.matrix.framework.commons.collection.CollectionUtil;
import wang.liangchen.matrix.framework.commons.enumeration.Symbol;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Liangchen.Wang
 */
@Service
public class TaskManager {
    private final static Logger logger = LoggerFactory.getLogger(TaskManager.class);
    private final StandaloneDao repository;

    @Inject
    public TaskManager(StandaloneDao repository) {
        this.repository = repository;
    }

    public int createTask(Wal wal, Host host) {
        Byte shardingNumber = wal.getShardingNumber();
        shardingNumber = shardingNumber == 0 ? 1 : shardingNumber;
        List<Task> tasks = new ArrayList<>();
        for (byte i = 0; i < shardingNumber; i++) {
            Task task = Task.newInstance();
            task.setParentId(0L);
            task.setWalId(wal.getWalId());
            task.setHostId(host.getHostId());
            task.setTriggerId(wal.getTriggerId());
            task.setExpectedHost("");
            task.setActualHost("");
            task.setTaskGroup(wal.getWalGroup());
            task.setTriggerParams(wal.getTriggerParams());
            task.setTaskParams(wal.getTaskParams());

            task.setShardingNumber(i);

            LocalDateTime now = LocalDateTime.now();
            task.setCreateDatetime(now);
            task.setAssignDatetime(now);
            task.setAckDatetime(now);
            task.setCompleteDatetime(now);
            task.setCompleteSummary(Symbol.BLANK.getSymbol());
            task.setProgress((short) 0);
            task.setState(TaskState.UNASSIGNED.getState());
            tasks.add(task);
        }
        return repository.insert(tasks);
    }

    public List<Task> assignTask(TaskReport taskReport) {
        // 获取所有可能的任务
        Criteria<Task> criteria = Criteria.of(Task.class)
                .resultFields(Task::getTaskId, Task::getExpectedHost, Task::getCreateDatetime)
                ._equals(Task::getTaskGroup, taskReport.getTaskGroup());
        List<Task> tasks = this.repository.list(criteria);
        if (CollectionUtil.INSTANCE.isEmpty(tasks)) {
            return Collections.EMPTY_LIST;
        }
        // 根据ExpectedHost分组
        Map<String, List<Task>> taskMap = tasks.stream().collect(Collectors.groupingBy(Task::getExpectedHost));
        tasks.clear();
        String hostLabel = taskReport.getHostLabel();
        List<Task> candidateTasks = taskMap.get(hostLabel);
        candidateTasks.sort(taskComparator());
        List<Task> sharedTasks = taskMap.get(Symbol.BLANK.getSymbol());
        sharedTasks.sort(taskComparator());
        candidateTasks.addAll(sharedTasks);

        Byte number = taskReport.getNumber();
        // exclusive tasks
        candidateTasks.forEach(task -> {
            if (tasks.size() < number) {
                exclusiveAndPopulateTask(task.getTaskId(), hostLabel, tasks);
            }
        });
        return tasks;
    }

    public void ackTask(Long taskId) {
        Task task = Task.newInstance();
        task.setState(TaskState.ACKED.getState());
        task.setAckDatetime(LocalDateTime.now());

        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(task)
                ._equals(Task::getTaskId, taskId)
                ._equals(Task::getState, TaskState.ASSIGNED.getState());
        this.repository.update(updateCriteria);
    }

    public void startTask(Long taskId) {
        Task task = Task.newInstance();
        task.setState(TaskState.EXECUTING.getState());

        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(task)
                ._equals(Task::getTaskId, taskId)
                ._equals(Task::getState, TaskState.ACKED.getState());
        this.repository.update(updateCriteria);
    }


    public void completeTask(TaskReport taskReport) {
        Task task = Task.newInstance();
        if (taskReport.getAborted()) {
            task.setState(TaskState.ABORTED.getState());
        } else {
            task.setState(TaskState.COMPLETED.getState());
        }

        task.setCompleteDatetime(LocalDateTime.now());
        task.setCompleteSummary(taskReport.getCompleteSummary());

        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(task)
                ._equals(Task::getTaskId, taskReport.getTaskId())
                ._equals(Task::getState, TaskState.EXECUTING.getState());
        this.repository.update(updateCriteria);
    }

    private void exclusiveAndPopulateTask(Long taskId, String hostLabel, List<Task> tasks) {
        Task task = Task.newInstance();
        task.setState(TaskState.ASSIGNED.getState());
        task.setActualHost(hostLabel);
        task.setAssignDatetime(LocalDateTime.now());

        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(task)
                ._equals(Task::getTaskId, taskId)
                ._equals(Task::getState, TaskState.UNASSIGNED.getState());
        int rows = this.repository.update(updateCriteria);
        // 抢占失败
        if (0 == rows) {
            logger.info("Exclusive Task failed,task:{}", taskId);
            return;
        }
        // 抢占成功
        Criteria<Task> criteria = Criteria.of(Task.class)
                .resultFields(Task::getTaskId, Task::getTriggerParams, Task::getTaskParams, Task::getShardingNumber)
                ._equals(Task::getTaskId, taskId);
        task = this.repository.select(criteria);
        tasks.add(task);
    }

    private Comparator<Task> taskComparator() {
        return (first, second) -> first.getCreateDatetime().isEqual(second.getCreateDatetime()) ? 0 : first.getCreateDatetime().isBefore(second.getCreateDatetime()) ? -1 : 1;
    }

}