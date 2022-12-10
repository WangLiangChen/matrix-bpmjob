package wang.liangchen.matrix.bpmjob.domain.host;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;
import wang.liangchen.matrix.framework.ddd.domain.AggregateRoot;

import java.time.LocalDateTime;

/**
 * 主机注册:TRIGGER;CONSOLE;CONSUMER;
    每次启动都新注册一个
 * @author Liangchen.Wang 2022-11-08 13:24:45
 */
@AggregateRoot
@Entity(name = "bpmjob_host")
public class Host extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    @Column(name = "host_id")
    private Long hostId;
    /**
     * TRIGGER;CONSOLE;CONSUMER
     */
    @Column(name = "host_type")
    private String hostType;
    /**
     * 分组标识{tenantCode}-{consumerCode}
     */
    @Column(name = "host_group")
    private String hostGroup;
    /**
     * 标识 如hostname等
     */
    @Column(name = "host_label")
    private String hostLabel;
    /**
     * 节点可以用于通讯的ip地址
     */
    @Column(name = "host_ip")
    private String hostIp;
    /**
     * 节点可以用于通讯的端口
     */
    @Column(name = "host_port")
    private Short hostPort;
    /**
     * 心跳间隔单位(S)
     */
    @Column(name = "heartbeat_interval")
    private Short heartbeatInterval;
    /**
     * 上线时间
     */
    @Column(name = "online_datetime")
    private LocalDateTime onlineDatetime;
    /**
     * 心跳时间
     */
    @Column(name = "heartbeat_datetime")
    private LocalDateTime heartbeatDatetime;
    /**
     * 离线时间
     */
    @Column(name = "offline_datetime")
    private LocalDateTime offlineDatetime;
    /**
     * 僵死时间
     */
    @Column(name = "dead_datetime")
    private LocalDateTime deadDatetime;
    /**
     * 节点终结者registry_id
     */
    @Column(name = "terminator")
    private Long terminator;
    /**
     * 节点状态1-在线 2-离线 4-僵死
     * 状态列
     */
    @ColumnState
    @Column(name = "state")
    private Byte state;

    public static Host valueOf(Object source) {
        return ObjectUtil.INSTANCE.copyProperties(source, Host.class);
    }

    public static Host newInstance() {
        return ClassUtil.INSTANCE.instantiate(Host.class);
    }
    public static Host newInstance(boolean initializeFields) {
        Host entity = ClassUtil.INSTANCE.instantiate(Host.class);
        if(initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

    public Long getHostId() {
        return this.hostId;
    }
    public void setHostId(Long hostId) {
        this.hostId = hostId;
    }
    public String getHostType() {
        return this.hostType;
    }
    public void setHostType(String hostType) {
        this.hostType = hostType;
    }
    public String getHostGroup() {
        return this.hostGroup;
    }
    public void setHostGroup(String hostGroup) {
        this.hostGroup = hostGroup;
    }
    public String getHostLabel() {
        return this.hostLabel;
    }
    public void setHostLabel(String hostLabel) {
        this.hostLabel = hostLabel;
    }
    public String getHostIp() {
        return this.hostIp;
    }
    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }
    public Short getHostPort() {
        return this.hostPort;
    }
    public void setHostPort(Short hostPort) {
        this.hostPort = hostPort;
    }
    public Short getHeartbeatInterval() {
        return this.heartbeatInterval;
    }
    public void setHeartbeatInterval(Short heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
    public LocalDateTime getOnlineDatetime() {
        return this.onlineDatetime;
    }
    public void setOnlineDatetime(LocalDateTime onlineDatetime) {
        this.onlineDatetime = onlineDatetime;
    }
    public LocalDateTime getHeartbeatDatetime() {
        return this.heartbeatDatetime;
    }
    public void setHeartbeatDatetime(LocalDateTime heartbeatDatetime) {
        this.heartbeatDatetime = heartbeatDatetime;
    }
    public LocalDateTime getOfflineDatetime() {
        return this.offlineDatetime;
    }
    public void setOfflineDatetime(LocalDateTime offlineDatetime) {
        this.offlineDatetime = offlineDatetime;
    }
    public LocalDateTime getDeadDatetime() {
        return this.deadDatetime;
    }
    public void setDeadDatetime(LocalDateTime deadDatetime) {
        this.deadDatetime = deadDatetime;
    }
    public Long getTerminator() {
        return this.terminator;
    }
    public void setTerminator(Long terminator) {
        this.terminator = terminator;
    }
    public Byte getState() {
        return this.state;
    }
    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Host{");
        builder.append("hostId = ").append(hostId).append(", ");
        builder.append("hostType = ").append(hostType).append(", ");
        builder.append("hostGroup = ").append(hostGroup).append(", ");
        builder.append("hostLabel = ").append(hostLabel).append(", ");
        builder.append("hostIp = ").append(hostIp).append(", ");
        builder.append("hostPort = ").append(hostPort).append(", ");
        builder.append("heartbeatInterval = ").append(heartbeatInterval).append(", ");
        builder.append("onlineDatetime = ").append(onlineDatetime).append(", ");
        builder.append("heartbeatDatetime = ").append(heartbeatDatetime).append(", ");
        builder.append("offlineDatetime = ").append(offlineDatetime).append(", ");
        builder.append("deadDatetime = ").append(deadDatetime).append(", ");
        builder.append("terminator = ").append(terminator).append(", ");
        builder.append("state = ").append(state).append(", ");
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }
}