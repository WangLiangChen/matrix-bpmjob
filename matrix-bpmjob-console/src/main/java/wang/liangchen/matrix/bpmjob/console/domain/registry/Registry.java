package wang.liangchen.matrix.bpmjob.console.domain.registry;

import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.IdStrategy;
import wang.liangchen.matrix.framework.data.annotation.ColumnState;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;
import wang.liangchen.matrix.framework.ddd.domain.AggregateRoot;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 触发引擎的节点和客户端节点注册
每次启动都是一个新的实例
 * @author Liangchen.Wang 2022-10-19 19:14:35
 */
@AggregateRoot
@Entity(name = "bpmjob_registry")
public class Registry extends RootEntity {
    /**
     * PrimaryKey
     */
    @Id
    @IdStrategy(IdStrategy.Strategy.MatrixFlake)
    @Column(name = "registry_id")
    private Long registryId;
    /**
     * 节点标识hostname_ip
     */
    @Column(name = "hostname")
    private String hostname;
    /**
     * 节点可以用于通讯的ip地址
     */
    @Column(name = "ip")
    private String ip;
    /**
     * 节点可以用于通讯的端口
     */
    @Column(name = "port")
    private Short port;
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
        * 版本列
        * 更新和删除时,非空则启用乐观锁
     */
    @ColumnState
    @Column(name = "state")
    private Short state;

    public static Registry valueOf(Object source) {
        return ObjectUtil.INSTANCE.copyProperties(source, Registry.class);
    }

    public static Registry newInstance() {
        return ClassUtil.INSTANCE.instantiate(Registry.class);
    }
    public static Registry newInstance(boolean initializeFields) {
        Registry entity = ClassUtil.INSTANCE.instantiate(Registry.class);
        if(initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

    public Long getRegistryId() {
        return this.registryId;
    }
    public void setRegistryId(Long registryId) {
        this.registryId = registryId;
    }
    public String getHostname() {
        return this.hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public String getIp() {
        return this.ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public Short getPort() {
        return this.port;
    }
    public void setPort(Short port) {
        this.port = port;
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
    public Short getState() {
        return this.state;
    }
    public void setState(Short state) {
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Registry{");
        builder.append("registryId = ").append(registryId).append(", ");
        builder.append("hostname = ").append(hostname).append(", ");
        builder.append("ip = ").append(ip).append(", ");
        builder.append("port = ").append(port).append(", ");
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