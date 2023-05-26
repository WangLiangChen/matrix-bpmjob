package wang.liangchen.matrix.bpmjob.domain.task;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import wang.liangchen.matrix.bpmjob.domain.trigger.Wal;
import wang.liangchen.matrix.framework.commons.collection.CollectionUtil;
import wang.liangchen.matrix.framework.commons.enumeration.Symbol;
import wang.liangchen.matrix.framework.data.dao.StandaloneDao;
import wang.liangchen.matrix.framework.data.dao.criteria.Criteria;
import wang.liangchen.matrix.framework.data.dao.criteria.UpdateCriteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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

    public int create(String hostLable, Wal wal) {
        Byte shardingNumber = wal.getShardingNumber();
        shardingNumber = shardingNumber == 0 ? 1 : shardingNumber;
        List<Task> tasks = new ArrayList<>();
        for (byte i = 0; i < shardingNumber; i++) {
            Task task = Task.newInstance();
            task.setParentId(0L);
            task.setWalId(wal.getWalId());
            task.setTriggerId(wal.getTriggerId());
            task.setExpectedHost(wal.getHostLabel());
            task.setActualHost(hostLable);
            task.setTaskGroup(wal.getWalGroup());
            task.setExecutorType(wal.getExecutorType());
            task.setExecutorOption(wal.getExecutorOption());
            task.setTriggerParams(wal.getTriggerParams());
            task.setTaskParams(wal.getTaskParams());

            task.setShardingNumber(i);

            LocalDateTime now = LocalDateTime.now();
            task.setExpectedDatetime(wal.getExpectedDatetime());
            task.setCreateDatetime(now);
            task.setAssignDatetime(now);
            task.setAcceptDatetime(now);
            task.setCompleteDatetime(now);
            task.setCompleteSummary(Symbol.BLANK.getSymbol());
            task.setProgress((byte) 0);
            task.setState(TaskState.UNASSIGNED.getState());
            tasks.add(task);
        }
        return repository.insert(tasks);
    }

    public List<Task> assign(TaskReport taskReport) {
        // 获取UNASSIGNED和ASSIGNED的任务
        Criteria<Task> criteria = Criteria.of(Task.class)
                .resultFields(Task::getTaskId, Task::getState, Task::getAssignDatetime)
                .pageSize(200)
                ._equals(Task::getTaskGroup, taskReport.getTaskGroup())
                ._in(Task::getState, TaskState.UNASSIGNED.getState(), TaskState.ASSIGNED.getState());
        List<Task> tasks = this.repository.list(criteria);
        if (CollectionUtil.INSTANCE.isEmpty(tasks)) {
            return Collections.emptyList();
        }
        LocalDateTime now = LocalDateTime.now();
        List<Task> candidateTasks = new ArrayList<>();
        String hostLabel = taskReport.getHostLabel();
        tasks.forEach(task -> {
            // 已经assign但超过5S未accept的;客户端设置超时2S;
            if (TaskState.ASSIGNED.getState() == task.getState() && now.minusSeconds(5).isAfter(task.getAssignDatetime())) {
                candidateTasks.add(task);
                return;
            }
            String expectedHost = task.getExpectedHost();
            if (null == expectedHost || expectedHost.isBlank() || hostLabel.equals(expectedHost)) {
                candidateTasks.add(task);
            }
        });

        tasks.clear();
        Byte number = taskReport.getNumber();
        // exclusive tasks
        candidateTasks.forEach(task -> {
            if (tasks.size() < number) {
                exclusiveAndPopulateTask(task.getTaskId(), hostLabel, tasks);
            }
        });
        return tasks;
    }

    public void accept(List<Long> taskIds) {
        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(Task.class)
                .forceUpdate(Task::getState, TaskState.ACCEPTED.getState())
                .forceUpdate(Task::getAcceptDatetime, LocalDateTime.now())
                ._in(Task::getTaskId, taskIds);
        this.repository.update(updateCriteria);
    }

    public void run(Long taskId) {
        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(Task.class)
                .forceUpdate(Task::getState, TaskState.EXECUTING.getState())
                ._equals(Task::getTaskId, taskId);
        this.repository.update(updateCriteria);
    }

    public void process(Long taskId, Byte process) {
        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(Task.class)
                .forceUpdate(Task::getProgress, process)
                ._equals(Task::getTaskId, taskId);
        this.repository.update(updateCriteria);
    }

    public void complete(TaskReport taskReport) {
        Task task = Task.newInstance();
        if (taskReport.getAborted()) {
            task.setState(TaskState.ABORTED.getState());
        } else {
            task.setState(TaskState.COMPLETED.getState());
        }
        task.setCompleteDatetime(LocalDateTime.now());
        task.setCompleteSummary(taskReport.getCompleteSummary());

        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(Task.class)
                .nonNullUpdate(task)
                ._equals(Task::getTaskId, taskReport.getTaskId());
        this.repository.update(updateCriteria);
    }

    private void exclusiveAndPopulateTask(Long taskId, String hostLabel, List<Task> tasks) {
        Task task = Task.newInstance();
        task.setState(TaskState.ASSIGNED.getState());
        task.setActualHost(hostLabel);
        task.setAssignDatetime(LocalDateTime.now());
        // 通过状态迁移抢占 UNASSIGNED-->ASSIGNED || ASSIGNED-->ASSIGNED
        UpdateCriteria<Task> updateCriteria = UpdateCriteria.of(task)
                ._equals(Task::getTaskId, taskId)
                ._in(Task::getState, TaskState.UNASSIGNED.getState(), TaskState.ASSIGNED.getState());
        int rows = this.repository.update(updateCriteria);
        // 抢占失败
        if (0 == rows) {
            logger.info("Exclusive Task failed, task:{}", taskId);
            return;
        }
        // 抢占成功
        logger.info("Exclusive Task success, task:{}", taskId);
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