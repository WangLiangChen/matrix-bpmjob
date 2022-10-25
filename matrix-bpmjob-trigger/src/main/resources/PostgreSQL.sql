/*==============================================================*/
/* Table: bpmjob_registry                                       */
/*==============================================================*/
create table bpmjob_registry
(
    registry_id        int8         not null,
    host_type          varchar(36)  not null,
    host_name          varchar(100) not null,
    host_ip            varchar(36)  not null,
    host_port          int2         not null,
    heartbeat_interval int2         not null,
    online_datetime    timestamp    not null,
    heartbeat_datetime timestamp    not null,
    offline_datetime   timestamp    null,
    dead_datetime      timestamp    null,
    terminator         int8         not null,
    state              int2         not null
);

comment on table bpmjob_registry is
    '触发引擎/控制台/客户端节点注册
    每次启动都是一个新的实例';

comment on column bpmjob_registry.registry_id is
    'PrimaryKey';

comment on column bpmjob_registry.host_type is
    '节点类型TRIGGER;CONSOLE;CLIENT';

comment on column bpmjob_registry.host_name is
    '节点标识hostname_ip';

comment on column bpmjob_registry.host_ip is
    '节点可以用于通讯的ip地址';

comment on column bpmjob_registry.host_port is
    '节点可以用于通讯的端口';

comment on column bpmjob_registry.heartbeat_interval is
    '心跳间隔单位(S)';

comment on column bpmjob_registry.online_datetime is
    '上线时间';

comment on column bpmjob_registry.heartbeat_datetime is
    '心跳时间';

comment on column bpmjob_registry.offline_datetime is
    '离线时间';

comment on column bpmjob_registry.dead_datetime is
    '僵死时间';

comment on column bpmjob_registry.terminator is
    '节点终结者registry_id';

comment on column bpmjob_registry.state is
    '节点状态1-在线 2-离线 4-僵死';

alter table bpmjob_registry
    add constraint pk_bpmjob_registry primary key (registry_id);

/*==============================================================*/
/* Index: bpmjob_registry_pk                                    */
/*==============================================================*/
create unique index bpmjob_registry_pk on bpmjob_registry (
                                                           registry_id
    );

/*==============================================================*/
/* Table: bpmjob_trigger                                        */
/*==============================================================*/
create table bpmjob_trigger
(
    trigger_id         int8         not null,
    trigger_name       varchar(36)  not null,
    trigger_group      varchar(36)  not null,
    trigger_type       varchar(20)  not null,
    trigger_expression varchar(36)  not null,
    trigger_previous   timestamp    not null,
    trigger_last       timestamp    not null,
    trigger_next       timestamp    not null,
    misfire_strategy   varchar(36)  not null,
    block_strategy     varchar(36)  not null,
    version            int4         not null default 0,
    owner              varchar(60)  not null default '',
    creator            varchar(60)  not null default '',
    create_datetime    timestamp    not null,
    modifier           varchar(60)  not null default '',
    modify_datetime    timestamp    not null,
    summary            varchar(200) not null default '',
    state              varchar(36)  not null
);

comment on table bpmjob_trigger is
    '触发器';

comment on column bpmjob_trigger.trigger_id is
    'PrimaryKey';

comment on column bpmjob_trigger.trigger_name is
    '名称';

comment on column bpmjob_trigger.trigger_group is
    '所属组,触发器划分的维度';

comment on column bpmjob_trigger.trigger_type is
    '触发器类型:FIXRATE;CRON;';

comment on column bpmjob_trigger.trigger_expression is
    '不同的触发器类型对应的表达式FIXRATE:1S 1M 1H 1D';

comment on column bpmjob_trigger.trigger_previous is
    '上次预期触发时间';

comment on column bpmjob_trigger.trigger_last is
    '末次实际触发时间';

comment on column bpmjob_trigger.trigger_next is
    '下次预期触发时间';

comment on column bpmjob_trigger.misfire_strategy is
    '触发错失处理策略';

comment on column bpmjob_trigger.block_strategy is
    '任务阻塞策略';

alter table bpmjob_trigger
    add constraint pk_bpmjob_trigger primary key (trigger_id);

/*==============================================================*/
/* Index: bpmjob_trigger_pk                                     */
/*==============================================================*/
create unique index bpmjob_trigger_pk on bpmjob_trigger (
                                                         trigger_id
    );
