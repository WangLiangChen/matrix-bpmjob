/*==============================================================*/
/* Table: bpmjob_consumer                                       */
/*==============================================================*/
create table if not exists bpmjob_consumer
(
    consumer_id     int8         not null,
    tenant_id       int8         null,
    tenant_code     varchar(36)  not null,
    consumer_code   varchar(36)  not null,
    consumer_name   varchar(36)  not null,
    constraint pk_bpmjob_consumer primary key (consumer_id),
    version         int4         not null default 0,
    owner           varchar(60)  not null default '',
    creator         varchar(60)  not null default '',
    create_datetime timestamp    not null,
    modifier        varchar(60)  not null default '',
    modify_datetime timestamp    not null,
    summary         varchar(200) not null default '',
    state           varchar(36)  not null
);

comment on table bpmjob_consumer is
    'app/client/consumer';

comment on column bpmjob_consumer.consumer_id is
    'PrimaryKey';

comment on column bpmjob_consumer.tenant_id is
    'PrimaryKey';

comment on column bpmjob_consumer.tenant_code is
    '租户唯一标识';

comment on column bpmjob_consumer.consumer_code is
    '唯一标识';

comment on column bpmjob_consumer.consumer_name is
    '名称';

/*==============================================================*/
/* Index: bpmjob_consumer_pk                                    */
/*==============================================================*/
create unique index if not exists bpmjob_consumer_pk on bpmjob_consumer (
                                                                         consumer_id
    );

/*==============================================================*/
/* Index: tenant_consumer_fk                                    */
/*==============================================================*/
create index if not exists tenant_consumer_fk on bpmjob_consumer (
                                                                  tenant_id
    );

/*==============================================================*/
/* Index: bpmjob_consumer_ak                                    */
/*==============================================================*/
create unique index if not exists bpmjob_consumer_ak on bpmjob_consumer (
                                                                         tenant_code,
                                                                         consumer_code
    );

/*==============================================================*/
/* Table: bpmjob_host                                           */
/*==============================================================*/
create table if not exists bpmjob_host
(
    host_id            int8         not null,
    host_type          varchar(36)  not null,
    host_group         varchar(73)  not null,
    host_label         varchar(100) not null,
    host_ip            varchar(36)  not null,
    host_port          int2         not null,
    heartbeat_interval int2         not null,
    online_datetime    timestamp    not null,
    heartbeat_datetime timestamp    not null,
    offline_datetime   timestamp    not null,
    dead_datetime      timestamp    not null,
    terminator         int8         not null,
    state              int2         not null,
    constraint pk_bpmjob_host primary key (host_id)
);

comment on table bpmjob_host is
    '主机注册:TRIGGER;CONSOLE;CONSUMER;
    每次启动都新注册一个';

comment on column bpmjob_host.host_id is
    'PrimaryKey';

comment on column bpmjob_host.host_type is
    'TRIGGER;CONSOLE;CONSUMER';

comment on column bpmjob_host.host_group is
    '分组标识{tenantCode}-{consumerCode}';

comment on column bpmjob_host.host_label is
    '标识 如hostname等';

comment on column bpmjob_host.host_ip is
    '节点可以用于通讯的ip地址';

comment on column bpmjob_host.host_port is
    '节点可以用于通讯的端口';

comment on column bpmjob_host.heartbeat_interval is
    '心跳间隔单位(S)';

comment on column bpmjob_host.online_datetime is
    '上线时间';

comment on column bpmjob_host.heartbeat_datetime is
    '心跳时间';

comment on column bpmjob_host.offline_datetime is
    '离线时间';

comment on column bpmjob_host.dead_datetime is
    '僵死时间';

comment on column bpmjob_host.terminator is
    '节点终结者registry_id';

comment on column bpmjob_host.state is
    '节点状态1-在线 2-离线 4-僵死';

/*==============================================================*/
/* Index: bpmjob_host_pk                                        */
/*==============================================================*/
create unique index if not exists bpmjob_host_pk on bpmjob_host (
                                                                 host_id
    );

/*==============================================================*/
/* Table: bpmjob_task                                           */
/*==============================================================*/
create table if not exists bpmjob_task
(
    task_id           int8          not null,
    host_id           int8          null,
    trigger_id        int8          null,
    parent_id         int8          null,
    task_group        varchar(73)   not null,
    trigger_params    varchar(1000) not null,
    task_params       varchar(1000) not null,
    parent_params     varchar(1000) not null,
    create_datetime   timestamp     not null,
    assign_datetime   timestamp     not null,
    ack_datetime      timestamp     not null,
    complete_datetime timestamp     not null,
    progress          int2          not null,
    state             int2          not null,
    constraint pk_bpmjob_task primary key (task_id)
);

comment on table bpmjob_task is
    '触发任务';

comment on column bpmjob_task.task_id is
    'PrimaryKey';

comment on column bpmjob_task.host_id is
    'PrimaryKey';

comment on column bpmjob_task.trigger_id is
    'PrimaryKey';

comment on column bpmjob_task.parent_id is
    'PrimaryKey';

comment on column bpmjob_task.task_group is
    '分组标识{tenantCode}-{consumerCode}';

comment on column bpmjob_task.trigger_params is
    '触发器参数';

comment on column bpmjob_task.task_params is
    '任务参数';

comment on column bpmjob_task.parent_params is
    '父级任务/上一任务参数';

comment on column bpmjob_task.create_datetime is
    '创建时间';

comment on column bpmjob_task.assign_datetime is
    '分配时间';

comment on column bpmjob_task.ack_datetime is
    '分配后,消费端确认时间';

comment on column bpmjob_task.complete_datetime is
    '完成时间';

comment on column bpmjob_task.progress is
    '进度百分比-乘以100之后的值';

comment on column bpmjob_task.state is
    '状态';

/*==============================================================*/
/* Index: bpmjob_task_pk                                        */
/*==============================================================*/
create unique index if not exists bpmjob_task_pk on bpmjob_task (
                                                                 task_id
    );

/*==============================================================*/
/* Index: task_self_fk                                          */
/*==============================================================*/
create index if not exists task_self_fk on bpmjob_task (
                                                        parent_id
    );

/*==============================================================*/
/* Index: trigger_task_fk                                       */
/*==============================================================*/
create index if not exists trigger_task_fk on bpmjob_task (
                                                           trigger_id
    );

/*==============================================================*/
/* Index: host_task_fk                                          */
/*==============================================================*/
create index if not exists host_task_fk on bpmjob_task (
                                                        host_id
    );


/*==============================================================*/
/* Table: bpmjob_tenant                                         */
/*==============================================================*/
create table if not exists bpmjob_tenant
(
    tenant_id       int8         not null,
    tenant_code     varchar(36)  not null,
    tenant_name     varchar(36)  not null,
    constraint pk_bpmjob_tenant primary key (tenant_id),
    version         int4         not null default 0,
    owner           varchar(60)  not null default '',
    creator         varchar(60)  not null default '',
    create_datetime timestamp    not null,
    modifier        varchar(60)  not null default '',
    modify_datetime timestamp    not null,
    summary         varchar(200) not null default '',
    state           varchar(36)  not null
);

comment on table bpmjob_tenant is
    '租户';

comment on column bpmjob_tenant.tenant_id is
    'PrimaryKey';

comment on column bpmjob_tenant.tenant_code is
    '唯一标识';

comment on column bpmjob_tenant.tenant_name is
    '名称';

/*==============================================================*/
/* Index: bpmjob_tenant_pk                                      */
/*==============================================================*/
create unique index if not exists bpmjob_tenant_pk on bpmjob_tenant (
                                                                     tenant_id
    );

/*==============================================================*/
/* Index: bpmjob_tenant_ak                                      */
/*==============================================================*/
create unique index if not exists bpmjob_tenant_ak on bpmjob_tenant (
                                                                     tenant_code
    );

/*==============================================================*/
/* Table: bpmjob_trigger                                        */
/*==============================================================*/
create table if not exists bpmjob_trigger
(
    trigger_id         int8          not null,
    trigger_group      varchar(73)   not null,
    trigger_name       varchar(36)   not null,
    trigger_type       varchar(20)   not null,
    trigger_expression varchar(36)   not null,
    trigger_next       timestamp     not null,
    miss_threshold     int2          not null,
    miss_strategy      varchar(36)   not null,
    trigger_params     varchar(1000) not null,
    task_settings      varchar(1000) not null,
    extended_settings  varchar(1000) not null,
    constraint pk_bpmjob_trigger primary key (trigger_id),
    version            int4          not null default 0,
    owner              varchar(60)   not null default '',
    creator            varchar(60)   not null default '',
    create_datetime    timestamp     not null,
    modifier           varchar(60)   not null default '',
    modify_datetime    timestamp     not null,
    summary            varchar(200)  not null default '',
    state              varchar(36)   not null
);

comment on table bpmjob_trigger is
    '触发器';

comment on column bpmjob_trigger.trigger_id is
    'PrimaryKey';

comment on column bpmjob_trigger.trigger_group is
    '分组标识{tenantCode}-{consumerCode}';

comment on column bpmjob_trigger.trigger_name is
    '名称';

comment on column bpmjob_trigger.trigger_type is
    '触发器类型:FIXRATE;CRON;';

comment on column bpmjob_trigger.trigger_expression is
    '不同的触发器类型对应的表达式FIXRATE:1S 1M 1H 1D';

comment on column bpmjob_trigger.trigger_next is
    '下次预期触发时间';

comment on column bpmjob_trigger.miss_threshold is
    '错失触发的阈值,单位S';

comment on column bpmjob_trigger.miss_strategy is
    '触发错失处理策略';

comment on column bpmjob_trigger.trigger_params is
    '触发参数';

comment on column bpmjob_trigger.task_settings is
    '任务参数(类型、策略、配置等)';

comment on column bpmjob_trigger.extended_settings is
    '扩展配置';

/*==============================================================*/
/* Index: bpmjob_trigger_pk                                     */
/*==============================================================*/
create unique index if not exists bpmjob_trigger_pk on bpmjob_trigger (
                                                                       trigger_id
    );

/*==============================================================*/
/* Table: bpmjob_wal                                            */
/*==============================================================*/
create table if not exists bpmjob_wal
(
    wal_id            int8        not null,
    host_id           int8        null,
    trigger_id        int8        null,
    wal_group         varchar(73) not null,
    create_datetime   timestamp   not null,
    schedule_datetime timestamp   not null,
    trigger_datetime  timestamp   not null,
    state             int2        not null,
    constraint pk_bpmjob_wal primary key (wal_id)
);

comment on table bpmjob_wal is
    'Write-Ahead Logging';

comment on column bpmjob_wal.wal_id is
    'PrimaryKey';

comment on column bpmjob_wal.host_id is
    'PrimaryKey';

comment on column bpmjob_wal.trigger_id is
    'PrimaryKey';

comment on column bpmjob_wal.wal_group is
    '分组标识{tenantCode}-{consumerCode}';

comment on column bpmjob_wal.create_datetime is
    '创建时间';

comment on column bpmjob_wal.schedule_datetime is
    '预期触发时间';

comment on column bpmjob_wal.trigger_datetime is
    '实际触发时间';

comment on column bpmjob_wal.state is
    '1-acquired;2-triggered';

/*==============================================================*/
/* Index: bpmjob_wal_pk                                         */
/*==============================================================*/
create unique index if not exists bpmjob_wal_pk on bpmjob_wal (
                                                               wal_id
    );

/*==============================================================*/
/* Index: host_wal_fk                                           */
/*==============================================================*/
create index if not exists host_wal_fk on bpmjob_wal (
                                                      host_id
    );

/*==============================================================*/
/* Index: trigger_wal_fk                                        */
/*==============================================================*/
create index if not exists trigger_wal_fk on bpmjob_wal (
                                                         trigger_id
    );

