package wang.liangchen.matrix.bpmjob.sdk.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.liangchen.matrix.bpmjob.sdk.core.exception.BpmJobException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Liangchen.Wang 2023-02-08 14:31
 */
public class BpmJobClientProperties {
    private final static Logger logger = LoggerFactory.getLogger(BpmJobClientProperties.class);
    private static volatile boolean instantiated = false;
    private static BpmJobClientProperties instance;

    public static BpmJobClientProperties getInstance() {
        if (instantiated) {
            return instance;
        }
        synchronized (BpmJobClientProperties.class) {
            if (instantiated) {
                return instance;
            }
            try (InputStream resourceAsStream = BpmJobClientProperties.class.getClassLoader().getResourceAsStream("bpmjob.properties")) {
                if (null == resourceAsStream) {
                    logger.error("Read 'bpmjob.properties' error. The file does not exist.");
                    throw new BpmJobException("Read 'bpmjob.properties' error. The file does not exist.");
                }
                Properties properties = new Properties();
                properties.load(resourceAsStream);
                instance = new BpmJobClientProperties();
                instance.setTenantId(properties.getProperty("tenantId"));
                instance.setAppId(properties.getProperty("appId"));
                instantiated = true;
            } catch (IOException e) {
                logger.error("Read 'bpmjob.properties' error. {}", e.getMessage());
                throw new BpmJobException(e);
            }
        }
        return instance;
    }

    /**
     * 租户Id
     */
    private String tenantId;
    /**
     * 应用Id
     */
    private String appId;

    /**
     * 执行任务的线程数
     */
    private byte taskThreadNumber = 16;
    /**
     * 获取任务的时间间隔(S)
     */
    private byte taskAcquireInterval = 1;

    /**
     * 心跳间隔(S)
     */
    private byte heartbeatInterval = 5;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }


    public byte getTaskThreadNumber() {
        return taskThreadNumber;
    }

    public void setTaskThreadNumber(byte taskThreadNumber) {
        this.taskThreadNumber = taskThreadNumber;
    }

    public byte getTaskAcquireInterval() {
        return taskAcquireInterval;
    }

    public void setTaskAcquireInterval(byte taskAcquireInterval) {
        this.taskAcquireInterval = taskAcquireInterval;
    }

    public byte getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(byte heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
}
