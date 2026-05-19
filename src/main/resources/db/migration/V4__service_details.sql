-- Мета информация о сервисах

create table if not exists registry.service_grpc_proxy_details
(
    uid         uuid primary key,
    service_uid uuid unique not null references registry.service (uid) on delete cascade
);

create table if not exists registry.service_gateway_details
(
    uid          uuid primary key,
    service_uid  uuid unique    not null references registry.service (uid) on delete cascade,

    hermes_group varchar(128),
    snapshots    varchar(128)[] not null default '{}'::varchar(128)[]
);

create table if not exists registry.service_base_details
(
    uid          uuid primary key,
    service_uid  uuid unique    not null references registry.service (uid) on delete cascade,

    shards_count int,
    hermes_group varchar(128),
    snapshots    varchar(128)[] not null default '{}'::varchar(128)[]
);

create table if not exists registry.service_meta_details
(
    uid          uuid primary key,
    service_uid  uuid unique    not null references registry.service (uid) on delete cascade,

    hermes_group varchar(128),
    snapshots    varchar(128)[] not null default '{}'::varchar(128)[]
);

create table if not exists registry.service_yt_proxy_details
(
    uid         uuid primary key,
    service_uid uuid unique   not null references registry.service (uid) on delete cascade,

    tenants     varchar(32)[] not null default '{}'::varchar(32)[],
    handles     varchar(32)[] not null default '{}'::varchar(32)[]
);



