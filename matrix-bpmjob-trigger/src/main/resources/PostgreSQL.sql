/*==============================================================*/
/* Table: bpmjob_server                                         */
/*==============================================================*/
create table bpmjob_server
(
    server_id          INT8         not null,
    server_host        VARCHAR(100) not null,
    heartbeat_interval INT2         not null,
    online_datetime    TIMESTAMP    not null,
    heartbeat_datetime TIMESTAMP    not null,
    offline_datetime   TIMESTAMP    null,
    dead_datetime      TIMESTAMP    null,
    server_terminator  INT8         not null,
    server_state       INT2         not null,
    constraint PK_BPMJOB_SERVER primary key (server_id)
);

comment on table bpmjob_server is
    '触发引擎的节点注册 每次启动都是一个新的实例';

comment on column bpmjob_server.server_id is
    'PrimaryKey';

comment on column bpmjob_server.server_host is
    '节点标识hostname_ip';

comment on column bpmjob_server.heartbeat_interval is
    '心跳间隔单位(S)';

comment on column bpmjob_server.online_datetime is
    '上线时间';

comment on column bpmjob_server.heartbeat_datetime is
    '心跳时间';

comment on column bpmjob_server.offline_datetime is
    '离线时间';

comment on column bpmjob_server.dead_datetime is
    '僵死时间';

comment on column bpmjob_server.server_terminator is
    '节点终结者server_id';

comment on column bpmjob_server.server_state is
    '节点状态1-在线 2-离线 4-僵死';

/*==============================================================*/
/* Index: bpmjob_server_PK                                      */
/*==============================================================*/
create unique index bpmjob_server_PK on bpmjob_server (
                                                       server_id
    );