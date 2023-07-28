/*==============================================================*/
/* Table: bpmjob_consumer                                       */
/*==============================================================*/
create table if not exists bpmjob_consumer
(
    consumer_id      int8          not null,
    tenant_code      varchar(36)   not null,
    consumer_code    varchar(36)   not null,
    consumer_name    varchar(36)   not null,
    algorithm        varchar(36)   not null,
    secret           varchar(128)  not null,
    provider_public  varchar(1000) not null,
    provider_private varchar(2000) not null,
    consumer_public  varchar(1000) not null,
    consumer_private varchar(2000) not null,
    constraint pk_bpmjob_consumer primary key (consumer_id),
    constraint ak_bpmjob_consumer_unique_key unique (consumer_code),
    version          int4          not null default 0,
    owner            varchar(60)   not null default '',
    creator          varchar(60)   not null default '',
    create_datetime  timestamp     not null,
    modifier         varchar(60)   not null default '',
    modify_datetime  timestamp     not null,
    summary          varchar(200)  not null default '',
    state            varchar(36)   not null
);

comment on table bpmjob_consumer is '接入使用本服务的app';

comment on column bpmjob_consumer.consumer_id is 'Consumer PrimaryKey';

comment on column bpmjob_consumer.tenant_code is '租户标识';

comment on column bpmjob_consumer.consumer_code is '使用当前服务的应用/服务唯一标识';

comment on column bpmjob_consumer.consumer_name is '消费者名称';

comment on column bpmjob_consumer.algorithm is '签名验签算法';

comment on column bpmjob_consumer.secret is '密钥;HMAC的key';

comment on column bpmjob_consumer.provider_public is '服务提供者公钥';

comment on column bpmjob_consumer.provider_private is '服务提供者私钥';

comment on column bpmjob_consumer.consumer_public is '消费者公钥';

comment on column bpmjob_consumer.consumer_private is '消费者私钥';

/*==============================================================*/
/* Table: bpmjob_javabean_executor                              */
/*==============================================================*/
create table if not exists bpmjob_javabean_executor
(
    executor_id     int8         not null,
    executor_key    varchar(36)  not null,
    tenant_code     varchar(36)  not null,
    app_code        varchar(36)  not null,
    class_name      varchar(100) not null,
    method_name     varchar(100) not null,
    annotation_name varchar(100) not null,
    constraint pk_bpmjob_javabean_executor primary key (executor_id),
    constraint ak_bpmjob_javabean_executor_unique_key unique (tenant_code, app_code)
);

comment on table bpmjob_javabean_executor is 'Client上报的java bean executor';

comment on column bpmjob_javabean_executor.executor_id is 'PrimaryKey';

comment on column bpmjob_javabean_executor.executor_key is 'MD5(tenant_code,app_code,executor_type,executor_settings)';

comment on column bpmjob_javabean_executor.tenant_code is '租户标识';

comment on column bpmjob_javabean_executor.app_code is '应用/服务唯一标识';

comment on column bpmjob_javabean_executor.class_name is 'className';

comment on column bpmjob_javabean_executor.method_name is 'methodName';

comment on column bpmjob_javabean_executor.annotation_name is 'annotationName';

/*==============================================================*/
/* Table: bpmjob_task                                           */
/*==============================================================*/
create table if not exists bpmjob_task
(
    task_id                    int8          not null,
    parent_id                  int8          null,
    trigger_id                 int8          null,
    wal_id                     int8          null,
    tenant_code                varchar(36)   not null,
    app_code                   varchar(36)   not null,
    expected_host              varchar(100)  not null,
    actual_host                varchar(100)  not null,
    executor_type              varchar(20)   not null,
    executor_settings          varchar(200)  not null,
    script_code                varchar(2000) not null,
    trigger_params             varchar(1000) not null,
    task_params                varchar(1000) not null,
    running_duration_threshold int4          not null,
    running_duration           int8          not null,
    sharding_number            int2          not null,
    sharding_sequence          int2          not null,
    expected_datetime          timestamp     not null,
    create_datetime            timestamp     not null,
    start_datetime             timestamp     not null,
    assign_datetime            timestamp     not null,
    accept_datetime            timestamp     not null,
    complete_datetime          timestamp     not null,
    complete_summary           varchar(1000) not null,
    progress                   int2          not null,
    state                      int2          not null,
    constraint pk_bpmjob_task primary key (task_id)
);

comment on table bpmjob_task is '触发任务';

comment on column bpmjob_task.trigger_id is 'PrimaryKey';

comment on column bpmjob_task.wal_id is 'PrimaryKey';

comment on column bpmjob_task.tenant_code is '租户标识';

comment on column bpmjob_task.app_code is '应用/服务唯一标识';

comment on column bpmjob_task.expected_host is '预期分配到任务的executor';

comment on column bpmjob_task.actual_host is '实际分配到任务的executor';

comment on column bpmjob_task.executor_type is '执行器类型:JAVABEAN, JAVA, GROOVY, SHELL, POWERSHELL, NODEJS, PHP;';

comment on column bpmjob_task.executor_settings is '执行器配置,不同的执行器有不同的配置方法名/脚本名';

comment on column bpmjob_task.script_code is '脚本代码';

comment on column bpmjob_task.trigger_params is '配置在trigger上的参数-静态';

comment on column bpmjob_task.task_params is '显式创建任务的参数-动态,会覆盖合并trigger_params.如API触发、子任务、流程任务等';

comment on column bpmjob_task.running_duration_threshold is '任务的运行时长阈值,单位S';

comment on column bpmjob_task.running_duration is '任务的运行时长,单位MS';

comment on column bpmjob_task.sharding_number is '分片数量';

comment on column bpmjob_task.sharding_sequence is '分片序号';

comment on column bpmjob_task.expected_datetime is '预期触发时间';

comment on column bpmjob_task.create_datetime is '创建时间';

comment on column bpmjob_task.start_datetime is '开始时间,创建时间或者分配超时后的补发时间';

comment on column bpmjob_task.assign_datetime is '分配时间';

comment on column bpmjob_task.accept_datetime is '分配后,消费端确认时间';

comment on column bpmjob_task.complete_datetime is '完成时间';

comment on column bpmjob_task.complete_summary is '完成信息摘要(正常/错误)';

comment on column bpmjob_task.progress is '进度百分比-乘以100之后的值';

comment on column bpmjob_task.state is '状态';

/*==============================================================*/
/* Index: task_self_fk                                          */
/*==============================================================*/
create index if not exists task_self_fk on bpmjob_task (parent_id);

/*==============================================================*/
/* Index: trigger_task_fk                                       */
/*==============================================================*/
create index if not exists trigger_task_fk on bpmjob_task (trigger_id);

/*==============================================================*/
/* Index: wal_task_fk                                           */
/*==============================================================*/
create index if not exists wal_task_fk on bpmjob_task (wal_id);

/*==============================================================*/
/* Table: bpmjob_trigger                                        */
/*==============================================================*/
create table if not exists bpmjob_trigger
(
    trigger_id                 int8          not null,
    tenant_code                varchar(36)   not null,
    app_code                   varchar(36)   not null,
    trigger_name               varchar(36)   not null,
    trigger_type               varchar(20)   not null,
    trigger_cron               varchar(36)   not null,
    executor_type              varchar(20)   not null,
    executor_settings          varchar(200)  not null,
    script_code                varchar(2000) not null,
    trigger_params             varchar(1000) not null,
    missed_threshold           int2          not null,
    missed_strategy            varchar(36)   not null,
    assign_strategy            varchar(36)   not null,
    sharding_number            int2          not null,
    running_duration_threshold int4          not null,
    constraint pk_bpmjob_trigger primary key (trigger_id),
    version                    int4          not null default 0,
    owner                      varchar(60)   not null default '',
    creator                    varchar(60)   not null default '',
    create_datetime            timestamp     not null,
    modifier                   varchar(60)   not null default '',
    modify_datetime            timestamp     not null,
    summary                    varchar(200)  not null default '',
    state                      varchar(36)   not null
);

comment on table bpmjob_trigger is '触发器';

comment on column bpmjob_trigger.trigger_id is 'PrimaryKey';

comment on column bpmjob_trigger.tenant_code is '租户标识';

comment on column bpmjob_trigger.app_code is '应用/服务唯一标识';

comment on column bpmjob_trigger.trigger_name is '触发器名称';

comment on column bpmjob_trigger.trigger_type is '类型:API;CRON;FIXRATE;FIXDELAY';

comment on column bpmjob_trigger.trigger_cron is '不同的触发器类型对应的CRON表达式';

comment on column bpmjob_trigger.executor_type is '执行器类型:JAVABEAN, JAVA, GROOVY, SHELL, POWERSHELL, NODEJS, PHP;';

comment on column bpmjob_trigger.executor_settings is '执行器配置,不同的执行器有不同的配置如方法名/脚本名';

comment on column bpmjob_trigger.script_code is '脚本代码';

comment on column bpmjob_trigger.trigger_params is '触发参数';

comment on column bpmjob_trigger.missed_threshold is '错失触发的阈值,单位S';

comment on column bpmjob_trigger.missed_strategy is '触发错失处理策略';

comment on column bpmjob_trigger.assign_strategy is '任务分配策略RANDOM等';

comment on column bpmjob_trigger.sharding_number is '分片数最小为1,生成x个任务';

comment on column bpmjob_trigger.running_duration_threshold is '任务的运行时长告警阈值,单位S';

/*==============================================================*/
/* Table: bpmjob_trigger_time                                   */
/*==============================================================*/
create table if not exists bpmjob_trigger_time
(
    trigger_id      int8      not null,
    trigger_instant timestamp not null,
    constraint pk_bpmjob_trigger_time primary key (trigger_id)
);

comment on table bpmjob_trigger_time is 'trigger触发时刻,需特别注意数据的新增和删除';

comment on column bpmjob_trigger_time.trigger_id is 'PrimaryKey';

comment on column bpmjob_trigger_time.trigger_instant is '预期触发时间';

/*==============================================================*/
/* Table: bpmjob_wal                                            */
/*==============================================================*/
create table if not exists bpmjob_wal
(
    wal_id            int8          not null,
    trigger_id        int8          null,
    wal_key           varchar(50)   not null,
    host_label        varchar(100)  not null,
    task_params       varchar(1000) not null,
    expected_datetime timestamp     not null,
    create_datetime   timestamp     not null,
    state             int2          not null
);

comment on table bpmjob_wal is 'Write-Ahead Logging';

comment on column bpmjob_wal.wal_id is 'PrimaryKey';

comment on column bpmjob_wal.trigger_id is 'PrimaryKey';

comment on column bpmjob_wal.wal_key is 'Uniquekey标识唯一触发trigger_id:expected_datetime';

comment on column bpmjob_wal.host_label is '标识 如hostname等';

comment on column bpmjob_wal.task_params is '显式创建任务的参数-动态,会覆盖合并trigger_params.如API触发、子任务、流程任务等';

comment on column bpmjob_wal.expected_datetime is '预期触发时间';

comment on column bpmjob_wal.create_datetime is '创建时间';

comment on column bpmjob_wal.state is '状态0-pending;1-confirmed';

/*==============================================================*/
/* Index: trigger_wal_fk                                        */
/*==============================================================*/
create index if not exists trigger_wal_fk on bpmjob_wal (trigger_id);


/*====================add index================================*/
CREATE INDEX if not exists bpmjob_trigger_time_trigger_instant_idx ON bpmjob_trigger_time (trigger_instant DESC);
CREATE INDEX if not exists bpmjob_wal_create_datetime_idx ON bpmjob_wal (create_datetime ASC);

