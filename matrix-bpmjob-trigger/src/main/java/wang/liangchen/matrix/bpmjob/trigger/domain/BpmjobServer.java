package wang.liangchen.matrix.bpmjob.trigger.domain;

import wang.liangchen.matrix.framework.commons.object.ObjectUtil;
import wang.liangchen.matrix.framework.commons.type.ClassUtil;
import wang.liangchen.matrix.framework.data.annotation.ColumnMarkDelete;
import wang.liangchen.matrix.framework.data.annotation.ColumnJson;
import wang.liangchen.matrix.framework.data.dao.entity.RootEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author Liangchen.Wang
 */
@Entity(name = "bpmjob_server")
public class BpmjobServer extends RootEntity {
    @Id
    @Column(name = "server_id")
    private Long serverId;
    @Column(name = "server_host")
    private String serverHost;
    @Column(name = "heartbeat_interval")
    private Short heartbeatInterval;
    @Column(name = "online_datetime")
    private LocalDateTime onlineDatetime;
    @Column(name = "heartbeat_datetime")
    private LocalDateTime heartbeatDatetime;
    @Column(name = "offline_datetime")
    private LocalDateTime offlineDatetime;
    @Column(name = "dead_datetime")
    private LocalDateTime deadDatetime;
    @Column(name = "server_terminator")
    private Long serverTerminator;
    @Column(name = "server_state")
    private Byte serverState;

    public static BpmjobServer valueOf(Object source) {
        return ObjectUtil.INSTANCE.copyProperties(source, BpmjobServer.class);
    }

    public static BpmjobServer newInstance() {
        return ClassUtil.INSTANCE.instantiate(BpmjobServer.class);
    }
    public static BpmjobServer newInstance(boolean initializeFields) {
        BpmjobServer entity = ClassUtil.INSTANCE.instantiate(BpmjobServer.class);
        if(initializeFields) {
            entity.initializeFields();
        }
        return entity;
    }

    public Long getServerId() {
        return this.serverId;
    }
    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }
    public String getServerHost() {
        return this.serverHost;
    }
    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
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
    public Long getServerTerminator() {
        return this.serverTerminator;
    }
    public void setServerTerminator(Long serverTerminator) {
        this.serverTerminator = serverTerminator;
    }
    public Byte getServerState() {
        return this.serverState;
    }
    public void setServerState(Byte serverState) {
        this.serverState = serverState;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BpmjobServer{");
        builder.append("serverId = ").append(serverId).append(", ");
        builder.append("serverHost = ").append(serverHost).append(", ");
        builder.append("heartbeatInterval = ").append(heartbeatInterval).append(", ");
        builder.append("onlineDatetime = ").append(onlineDatetime).append(", ");
        builder.append("heartbeatDatetime = ").append(heartbeatDatetime).append(", ");
        builder.append("offlineDatetime = ").append(offlineDatetime).append(", ");
        builder.append("deadDatetime = ").append(deadDatetime).append(", ");
        builder.append("serverTerminator = ").append(serverTerminator).append(", ");
        builder.append("serverState = ").append(serverState).append(", ");
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }
}