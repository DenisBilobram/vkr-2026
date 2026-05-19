create type registry.service_type as enum (
    'GRPC_PROXY', 'GATEWAY', 'BASE', 'META', 'YT_PROXY'
    );

create table if not exists registry.service
(
    uid           uuid primary key,

    cloud_service varchar(64)           not null unique, -- vkvideo-gateway
    service_type  registry.service_type not null,

    -- сервис принадлежит либо проекту, либо вертикали
    project_uid   uuid references registry.project (uid) on delete cascade,
    vertical_uid  uuid references registry.vertical (uid) on delete cascade,

    constraint cloud_service_owner_chk
        check (
            (project_uid is not null and vertical_uid is null)
                or
            (project_uid is null and vertical_uid is not null)
            )
);

-- по одному типу сервиса на каждую вертикаль или проект
create unique index if not exists service_one_type_per_project_uq
    on registry.service (project_uid, service_type)
    where project_uid is not null;

create unique index if not exists service_one_type_per_vertical_uq
    on registry.service (vertical_uid, service_type)
    where vertical_uid is not null;

-- Таблицы для информации о one-cloud
create table if not exists registry.one_cloud_service_info
(
    uid            uuid primary key,
    service_uid    uuid         not null references registry.service (uid) on delete cascade,
    namespace      varchar(64)  not null,                           -- public
    cloud_queue    varchar(512) not null,                           -- discovery-portal-flow.sre.public.app.production.recommender.prod
    service_prefix varchar(64),                                     -- java., java-shard0

    dc             varchar(2)[] not null default '{}'::varchar(2)[] -- [HC, PC, UC]
);
