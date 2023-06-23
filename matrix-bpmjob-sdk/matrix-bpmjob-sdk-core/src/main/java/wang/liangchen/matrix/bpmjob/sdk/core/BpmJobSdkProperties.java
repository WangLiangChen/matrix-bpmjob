package wang.liangchen.matrix.bpmjob.sdk.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Liangchen.Wang 2023-02-08 14:31
 */
public class BpmJobSdkProperties {
    private final static Logger logger = LoggerFactory.getLogger(BpmJobSdkProperties.class);
    private static volatile boolean instantiated = false;
    private static BpmJobSdkProperties instance;

    public static BpmJobSdkProperties getInstance() {
        if (instantiated) {
            return instance;
        }
        synchronized (BpmJobSdkProperties.class) {
            if (instantiated) {
                return instance;
            }
            try (InputStream resourceAsStream = BpmJobSdkProperties.class.getClassLoader().getResourceAsStream("bpmjob.properties")) {
                if (null == resourceAsStream) {
                    logger.info("Read 'bpmjob.properties' error. The file does not exist.");
                } else {
                    Properties properties = new Properties();
                    properties.load(resourceAsStream);
                    String taskUri = properties.getProperty("taskUri");
                    if (null == taskUri || taskUri.isEmpty()) {
                        properties.setProperty("taskUri", properties.getProperty("uri"));
                    }
                    instance = new BpmJobSdkProperties();
                    instance.setTenantId(properties.getProperty("tenantId"));
                    instance.setAppId(properties.getProperty("appId"));
                    instance.setSecret(properties.getProperty("secret"));
                    instance.setUri(properties.getProperty("uri"));
                    instance.setTaskUri(properties.getProperty("taskUri"));
                    instantiated = true;
                }
            } catch (IOException e) {
                logger.info("Read 'bpmjob.properties' error. {}", e.getMessage());
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
     * 签名验签密钥
     */
    private String secret;
    /**
     * 请求服务端的URI
     */
    private String uri;
    /**
     * 请求任务独立的URI
     * 未设置则taskUri = uri
     */
    private String taskUri;

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

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTaskUri() {
        return taskUri;
    }

    public void setTaskUri(String taskUri) {
        this.taskUri = taskUri;
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
