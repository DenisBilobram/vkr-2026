create schema if not exists registry;

create table if not exists registry.project
(
    uid          uuid primary key,

    server_name  varchar(64) not null unique, -- vk-video, vk-clips, social
    display_name varchar(64) not null unique, -- VK Video, VK Clips, Social,

    abc_id       varchar(4)  not null unique, -- 1234, 5024, 9204
    prm          varchar(16) not null unique, -- PRM-123456
    weight       real        not null,        -- 0.0 - 1.0

    constraint project_abc_id_format_chk
        check (abc_id ~ '^[0-9]{4}$'),

    constraint project_weight_range_chk
        check (weight >= 0 and weight <= 1)
);

create type registry.release_status as enum (
    'incubating',
    'testing',
    'prod'
    );

create table if not exists registry.vertical
(
    uid            uuid primary key,
    project_uid    uuid                    not null references registry.project (uid) on delete cascade,

    server_name    varchar(64)             not null, -- vk-video, vk-clips
    display_name   varchar(64)             not null, -- VK Video, VK Clips

    abc_id         varchar(4)              not null, -- 1234, 5024, 9204
    prm            varchar(16)             not null, -- PRM-123456
    weight         numeric(4, 3)           not null, -- 0.0 - 1.0

    release_status registry.release_status not null default 'prod',

    unique (project_uid, server_name),
    unique (project_uid, display_name),

    constraint vertical_abc_id_format_chk
        check (abc_id ~ '^[0-9]{4}$'),

    constraint vertical_weight_range_chk
        check (weight >= 0 and weight <= 1)
);
