package liangchen.wang.matrix.bpmjob.sdk.core;

/**
 * @author Liangchen.Wang 2023-02-08 14:31
 */
public class BpmJobSdkProperties {
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
    private byte heartbeatInterval = 10;
    /**
     * 设定的ip地址，不设定为自动获取
     */
    private String ip;
    private String serverURI;

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getServerURI() {
        return serverURI;
    }

    public void setServerURI(String serverURI) {
        this.serverURI = serverURI;
    }
}
