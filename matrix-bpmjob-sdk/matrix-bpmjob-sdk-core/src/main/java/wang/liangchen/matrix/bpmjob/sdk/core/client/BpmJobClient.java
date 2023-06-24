package wang.liangchen.matrix.bpmjob.sdk.core.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.bpmjob.api.ExecutorMethod;
import wang.liangchen.matrix.bpmjob.api.TaskResponse;
import wang.liangchen.matrix.bpmjob.sdk.core.BpmJobClientProperties;
import wang.liangchen.matrix.bpmjob.sdk.core.annotation.BpmJob;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.Connector;
import wang.liangchen.matrix.bpmjob.sdk.core.connector.ConnectorFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.exception.BpmJobException;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.BpmJobExecutor;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.BpmjobExecutorFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.executor.ExecutorType;
import wang.liangchen.matrix.bpmjob.sdk.core.runtime.ClassScanner;
import wang.liangchen.matrix.bpmjob.sdk.core.thread.BpmJobThread;
import wang.liangchen.matrix.bpmjob.sdk.core.thread.BpmJobThreadFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.thread.BpmJobThreadInfo;
import wang.liangchen.matrix.bpmjob.sdk.core.utils.ThreadSnapshot;

import java.lang.management.ThreadInfo;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Liangchen.Wang 2023-02-15 8:05
 */
public final class BpmJobClient {
    private final static Logger logger = LoggerFactory.getLogger(BpmJobClient.class);
    private final static AtomicInteger clientCounter = new AtomicInteger();
    private final static Map<ExecutorMethod, Method> bpmjobExecutors = new HashMap<>();
    private final String clientName;
    private final BpmJobClientProperties bpmJobClientProperties;
    private final Connector connector;
    private final Executor connectorPool;
    private final BpmjobExecutorFactory executorFactory;
    private boolean halted = true;
    private final AtomicInteger idleThreadCounter;
    private final ExecutorService fastTaskExecutorPool;
    private final ExecutorService slowTaskExecutorPool;
    private final ScheduledExecutorService taskScheduler;
    private final ScheduledExecutorService heartbeatScheduler;
    private final Map<ExecutorType, ThreadPoolExecutor> threadMonitors = new HashMap<>();


    static {
        // 实现BpmJobExecutor接口的方法
        Set<Class<?>> implementedClasses = ClassScanner.INSTANCE.getImplementedClasses(BpmJobExecutor.class);
        implementedClasses.stream().filter(clazz -> clazz != BpmJobExecutor.class).forEach(clazz -> {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            String className = clazz.getName();
            for (Method declaredMethod : declaredMethods) {
                String methodName = declaredMethod.getName();
                bpmjobExecutors.put(ExecutorMethod.newInstance(className, methodName, methodName), declaredMethod);
            }
        });
        // 注解的方法
        Set<Method> annotatedMethods = ClassScanner.INSTANCE.getAnnotatedMethods(BpmJob.class);
        for (Method annotatedMethod : annotatedMethods) {
            Class<?> clazz = annotatedMethod.getDeclaringClass();
            String className = clazz.getName();
            String methodName = annotatedMethod.getName();
            bpmjobExecutors.put(ExecutorMethod.newInstance(className, methodName, methodName), annotatedMethod);
            BpmJob annotation = annotatedMethod.getAnnotation(BpmJob.class);
            String value = annotation.value();
            if (null != value) {
                bpmjobExecutors.put(ExecutorMethod.newInstance(className, methodName, value), annotatedMethod);
            }
            for (String name : annotation.names()) {
                bpmjobExecutors.put(ExecutorMethod.newInstance(className, methodName, name), annotatedMethod);
            }
        }
    }

    public BpmJobClient(BpmJobClientProperties bpmJobClientProperties, ConnectorFactory connectorFactory, BpmjobExecutorFactory executorFactory) {
        this.bpmJobClientProperties = bpmJobClientProperties;
        this.connector = connectorFactory.createConnector();
        this.executorFactory = executorFactory;
        this.clientName = String.format("bpmjob-client-%d", clientCounter.getAndIncrement());

        this.connectorPool = Executors.newCachedThreadPool(new BpmJobThreadFactory("bpmjob-connector-", "bpmjob-connector"));
        this.taskScheduler = Executors.newScheduledThreadPool(1, new BpmJobThreadFactory("bpmjob-task-scheduler-", "bpmjob-task-scheduler"));
        this.heartbeatScheduler = Executors.newScheduledThreadPool(1, new BpmJobThreadFactory("bpmjob-heartbeat-scheduler-", "bpmjob-heartbeat-scheduler"));

        byte taskThreadNumber = this.bpmJobClientProperties.getTaskThreadNumber();
        this.idleThreadCounter = new AtomicInteger(taskThreadNumber);
        this.fastTaskExecutorPool = Executors.newFixedThreadPool(taskThreadNumber, new BpmJobThreadFactory("bpmjob-fasttask-", "bpmjob-fasttask"));
        this.slowTaskExecutorPool = Executors.newFixedThreadPool(taskThreadNumber, new BpmJobThreadFactory("bpmjob-slowtask-", "bpmjob-slowtask"));

        // add thread monitor
        if (this.taskScheduler instanceof ThreadPoolExecutor) {
            this.threadMonitors.put(ExecutorType.TASK_SCHEDULER, (ThreadPoolExecutor) this.taskScheduler);
        }
        if (this.heartbeatScheduler instanceof ThreadPoolExecutor) {
            this.threadMonitors.put(ExecutorType.HEARTBEAT_SCHEDULER, (ThreadPoolExecutor) this.heartbeatScheduler);
        }
        if (this.fastTaskExecutorPool instanceof ThreadPoolExecutor) {
            this.threadMonitors.put(ExecutorType.FAST_TASK_RUNNER, (ThreadPoolExecutor) this.fastTaskExecutorPool);
        }
        if (this.slowTaskExecutorPool instanceof ThreadPoolExecutor) {
            this.threadMonitors.put(ExecutorType.SLOW_TASK_RUNNER, (ThreadPoolExecutor) this.slowTaskExecutorPool);
        }
    }

    public synchronized void start() {
        if (this.halted) {
            this.heartbeatScheduler.schedule(new HeartbeatProcessor(), this.bpmJobClientProperties.getHeartbeatInterval(), TimeUnit.SECONDS);
            this.taskScheduler.schedule(new GetTaskProcessor(), this.bpmJobClientProperties.getTaskAcquireInterval(), TimeUnit.SECONDS);
            // register hooker
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
            this.halted = false;
            logger.info("BpmjobClient '{}' is running", this.clientName);
        }
    }

    public synchronized void shutdown() {
        if (this.halted) {
            return;
        }
        this.threadMonitors.forEach(((executorType, executor) -> {
            logger.info("shutdown executor:{}", executorType);
            shutdownExecutor(executor);
        }));
        logger.info("BpmjobClient '{}' is shutdown", this.clientName);
        this.halted = true;
    }

    private void shutdownExecutor(ThreadPoolExecutor executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("shutdown executor error.", e);
        }
    }

    private class GetTaskProcessor implements Runnable {
        @Override
        public void run() {
            int idleThreadNumber = idleThreadCounter.getAndSet(0);
            if (idleThreadNumber <= 0) {
                logger.info("No idle threads");
                return;
            }
            connector.getTasks(idleThreadNumber)
                    .whenComplete((tasks, throwable) -> {
                        if (null != throwable) {
                            idleThreadCounter.addAndGet(idleThreadNumber);
                            logger.error("get tasks error.", throwable);
                            return;
                        }
                        if (null == tasks || tasks.isEmpty()) {
                            idleThreadCounter.addAndGet(idleThreadNumber);
                            logger.info("No tasks got");
                            return;
                        }
                        int gotNumber = tasks.size();
                        int spareNumber = idleThreadNumber - gotNumber;
                        if (spareNumber > 0) {
                            idleThreadCounter.addAndGet(spareNumber);
                            logger.info("idleThreadNumber:{}, gotNumber:{}, spareNumber:{}", idleThreadNumber, gotNumber, spareNumber);
                        }
                        // accept
                        Set<Long> taskIds = tasks.stream().map(TaskResponse::getTaskId).collect(Collectors.toSet());
                        connector.acceptTasks(taskIds)
                                .whenComplete((empty, acceptThrowable) -> {
                                    if (null == acceptThrowable) {
                                        runTasks(tasks);
                                    } else {
                                        logger.error("accept tasks error.", acceptThrowable);
                                    }
                                });
                    });
        }

        private void runTasks(List<TaskResponse> tasks) {
            for (TaskResponse task : tasks) {
                ExecutorService executor = resolveExecutor(task);
                executor.execute(new RunTaskProcessor(task));
            }
        }

        private ExecutorService resolveExecutor(TaskResponse task) {
            // default use fastTaskExecutorPool
            return fastTaskExecutorPool;
        }
    }

    private class RunTaskProcessor implements Runnable {
        private final TaskResponse task;

        private RunTaskProcessor(TaskResponse task) {
            this.task = task;
        }

        @Override
        public void run() {
            Long taskId = task.getTaskId();
            String className = task.getClassName();
            Method method = bpmjobExecutors.get(ExecutorMethod.newInstance(className, task.getMethodName(), task.getAnnotationName()));
            if (null == method) {
                logger.error("The method doesn't exist.taskId:{}, className:{} ,methodName:{}, annotationName:{}",
                        taskId, className, task.getMethodName(), task.getAnnotationName());
                connector.completeTask(taskId, new BpmJobException("The method doesn't exist")).whenComplete((empty, throwable) -> {
                    if (null != throwable) {
                        logger.error("complete task error.taskId:".concat(String.valueOf(taskId)), throwable);
                    }
                });
                return;
            }
            // set taskId to Thread
            Thread thread = Thread.currentThread();
            if (thread instanceof BpmJobThread) {
                ((BpmJobThread) thread).setTaskId(task.getTaskId());
            }
            try {
                Object executor = executorFactory.createExecutor(className);
                method.invoke(executor, task.getJsonStringPatameter());
            } catch (Exception exception) {
                connector.completeTask(taskId, exception).whenComplete((empty, throwable) -> {
                    if (null != throwable) {
                        logger.error("complete task error.taskId:".concat(String.valueOf(taskId)), throwable);
                    }
                });
            } finally {
                idleThreadCounter.incrementAndGet();
                connector.completeTask(taskId).whenComplete((empty, throwable) -> {
                    if (null != throwable) {
                        logger.error("complete task error.taskId:".concat(String.valueOf(taskId)), throwable);
                    }
                });
            }
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

                }
            }));
        }
    }
}
