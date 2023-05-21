package liangchen.wang.matrix.bpmjob.sdk.core.client;


import io.netty.util.concurrent.DefaultThreadFactory;
import liangchen.wang.matrix.bpmjob.sdk.core.BpmJobSdkProperties;
import liangchen.wang.matrix.bpmjob.sdk.core.client.dto.TaskResponse;
import liangchen.wang.matrix.bpmjob.sdk.core.enums.ExecutorType;
import liangchen.wang.matrix.bpmjob.sdk.core.thread.BpmJobThreadFactory;
import liangchen.wang.matrix.bpmjob.sdk.core.thread.BpmJobThreadInfo;
import liangchen.wang.matrix.bpmjob.sdk.core.utils.ThreadSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ThreadInfo;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Liangchen.Wang 2023-02-15 8:05
 */
public final class BpmJobClient {
    private final static Logger logger = LoggerFactory.getLogger(BpmJobClient.class);
    private final static AtomicInteger clientCounter = new AtomicInteger();
    private final String clientName;
    private final BpmJobSdkProperties bpmJobSdkProperties;
    private final TaskClient taskClient;
    private volatile boolean halted = false;
    private final AtomicInteger idleThreadCounter;
    private final ExecutorService taskExecutor;
    private final ExecutorService slowTaskExecutor;
    private final ScheduledExecutorService getTaskExecutor;
    private final ScheduledExecutorService heartbeatExecutor;
    private final Map<ExecutorType, ThreadPoolExecutor> threadMonitors = new HashMap<>();

    public BpmJobClient(BpmJobSdkProperties bpmJobSdkProperties) {
        this.clientName = String.format("bpmjob-client-%d", clientCounter.getAndIncrement());
        this.bpmJobSdkProperties = bpmJobSdkProperties;
        this.taskClient = new TaskClient(this.bpmJobSdkProperties);
        byte taskThreadNumber = this.bpmJobSdkProperties.getTaskThreadNumber();
        this.idleThreadCounter = new AtomicInteger(taskThreadNumber);
        this.getTaskExecutor = Executors.newScheduledThreadPool(1, new BpmJobThreadFactory("bpmjob-getter-", "bpmjob-getter"));
        this.heartbeatExecutor = Executors.newScheduledThreadPool(1, new BpmJobThreadFactory("bpmjob-heartbeat-", "bpmjob-heartbeat"));
        this.taskExecutor = Executors.newFixedThreadPool(taskThreadNumber, new BpmJobThreadFactory("bpmjob-task-", "bpmjob-task"));
        this.slowTaskExecutor = Executors.newFixedThreadPool(taskThreadNumber, new BpmJobThreadFactory("bpmjob-slowtask-", "bpmjob-slowtask"));
        // add thread monitor
        if (getTaskExecutor instanceof ThreadPoolExecutor) {
            threadMonitors.put(ExecutorType.TASK_GETTER, (ThreadPoolExecutor) getTaskExecutor);
        }
        if (heartbeatExecutor instanceof ThreadPoolExecutor) {
            threadMonitors.put(ExecutorType.HEARTBEAT, (ThreadPoolExecutor) heartbeatExecutor);
        }
        if (taskExecutor instanceof ThreadPoolExecutor) {
            threadMonitors.put(ExecutorType.TASK_GETTER, (ThreadPoolExecutor) taskExecutor);
        }
        if (slowTaskExecutor instanceof ThreadPoolExecutor) {
            threadMonitors.put(ExecutorType.SLOW_TASK_RUNNER, (ThreadPoolExecutor) slowTaskExecutor);
        }
    }

    public void start() {
        // heartbeat
        this.heartbeatExecutor.schedule(new HeartbeatProcessor(), this.bpmJobSdkProperties.getHeartbeatInterval(), TimeUnit.SECONDS);
        // get task scheduler
        this.getTaskExecutor.schedule(new GetTaskProcessor(), this.bpmJobSdkProperties.getTaskAcquireInterval(), TimeUnit.SECONDS);
        // register hooker
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    private void shutdown() {
        this.halted = true;
        this.threadMonitors.forEach(((executorType, executor) -> {
            logger.info("shutdown executor:{}", executorType);
            shutdownExecutor(executor);
        }));
    }

    private void shutdownExecutor(ThreadPoolExecutor executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("shutdown executor error.", e);
        }
    }

    private class GetTaskProcessor implements Runnable {
        @Override
        public void run() {
            // 空闲线程数即可获取的任务数
            int idleThreadNumber = idleThreadCounter.getAndSet(0);
            if (idleThreadNumber <= 0) {
                logger.debug("No idle threads");
                return;
            }
            // 获取任务
            List<TaskResponse> tasks = getTasks(idleThreadNumber);
            // 接受任务
            boolean accepted = acceptTasks(tasks);
            if (accepted) {
                // 运行任务
                runTasks(tasks);
            }
        }

        private List<TaskResponse> getTasks(int idleThreadNumber) {
            try {
                List<TaskResponse> tasks = taskClient.getTasks(idleThreadNumber);
                int gettedNumber = null == tasks ? 0 : tasks.size();
                int unwantedNumber = idleThreadNumber - gettedNumber;
                if (unwantedNumber > 0) {
                    logger.debug("idleThreadNumber:{}, gettedNumber:{}, unwantedNumber:{}", idleThreadNumber, gettedNumber, unwantedNumber);
                    idleThreadCounter.addAndGet(unwantedNumber);
                }
                return tasks;
            } catch (Exception e) {
                logger.error("get tasks error.", e);
                idleThreadCounter.addAndGet(idleThreadNumber);
            }
            return Collections.emptyList();
        }

        private boolean acceptTasks(List<TaskResponse> tasks) {
            try {
                taskClient.acceptTasks(tasks.stream().map(TaskResponse::getTaskId).collect(Collectors.toSet()));
                return Boolean.TRUE;
            } catch (Exception e) {
                logger.error("accept tasks error.", e);
            }
            return Boolean.FALSE;
        }

        private void runTasks(List<TaskResponse> tasks) {
            for (TaskResponse task : tasks) {
                ExecutorService executor = resolveExecutor(task);
                executor.execute(new TaskProcessor(task, () -> {
                    // do anything
                }, () -> {
                    // report success
                    taskClient.completeTask(task.getTaskId(), null);
                }, exception -> {
                    logger.error("run task error.", exception);
                    // report failure
                    taskClient.completeTask(task.getTaskId(), exception);
                }));
            }
        }

        private ExecutorService resolveExecutor(TaskResponse task) {
            // default
            return taskExecutor;
        }
    }

    private class TaskProcessor implements Runnable {
        private final TaskResponse task;
        private final Runnable preProcessor;
        private final Runnable postProcessor;
        private final Consumer<Exception> errorHandler;

        private TaskProcessor(TaskResponse task, Runnable preProcessor, Runnable postProcessor, Consumer<Exception> errorHandler) {
            this.task = task;
            this.preProcessor = preProcessor;
            this.postProcessor = postProcessor;
            this.errorHandler = errorHandler;
        }

        @Override
        public void run() {
            Exception exception = null;
            // pre and run
            try {
                preProcessor.run();
                runTask(task);
            } catch (Exception e) {
                exception = e;
            } finally {
                // regardless of success and failure
                idleThreadCounter.incrementAndGet();
                try {
                    postProcessor.run();
                } catch (Exception e) {
                    exception = e;
                }
            }
            errorHandler.accept(exception);
        }

        private void runTask(TaskResponse task) {

        }
    }

    private class HeartbeatProcessor implements Runnable {
        @Override
        public void run() {
            threadMonitors.forEach(((executorType, executor) -> {
                ThreadFactory threadFactory = executor.getThreadFactory();
                if (threadFactory instanceof BpmJobThreadFactory) {
                    BpmJobThreadFactory bpmJobThreadFactory = (BpmJobThreadFactory) threadFactory;
                    ThreadGroup threadGroup = bpmJobThreadFactory.getThreadGroup();
                    List<ThreadInfo> threadInfos = ThreadSnapshot.INSTANCE.threadInfo(threadGroup);
                    List<BpmJobThreadInfo> bpmJobThreadInfos = threadInfos.stream().map(BpmJobThreadInfo::new).collect(Collectors.toList());
                    // TODO 怎么搞到taskId
                }
            }));
        }
    }
}
