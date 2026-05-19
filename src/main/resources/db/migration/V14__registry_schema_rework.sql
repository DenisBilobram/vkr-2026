DO $$
BEGIN
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'PLATFORM_GATEWAY';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'MEDIATOR';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'FACTOR_PROXY';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'SELECTORS';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'WORKER';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'SNAPSHOTS_BUILDER';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'META_I2I';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'BASE_I2I';
    ALTER TYPE registry.service_type ADD VALUE IF NOT EXISTS 'SCHEDULER_I2I';
END $$;

DO $$
BEGIN
    CREATE TYPE registry.datacenter_code AS ENUM ('pc', 'uc', 'hc', 'rc', 'kc', 'dc');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

ALTER TABLE registry.project
    RENAME COLUMN server_name TO project_name;

ALTER TABLE registry.vertical
    RENAME COLUMN server_name TO vertical_name;

ALTER TABLE registry.function
    RENAME COLUMN server_name TO function_name;

CREATE TABLE IF NOT EXISTS registry.project_general_info
(
    uid                    uuid primary key,
    project_uid            uuid not null unique references registry.project (uid) on delete cascade,
    display_name           varchar(64),
    product_id             integer,
    prm                    varchar(16),
    toggles_offline_tenant varchar(128),
    links                  jsonb not null default '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS registry.vertical_general_info
(
    uid                      uuid primary key,
    vertical_uid             uuid not null unique references registry.vertical (uid) on delete cascade,
    display_name             varchar(64),
    service_owner            varchar(128),
    product_id               integer,
    prm                      varchar(16),
    hermes_project_name      varchar(128),
    dictionary_base_project  varchar(128),
    additional_responsibles  text[] not null default '{}'::text[],
    additional_followers     text[] not null default '{}'::text[],
    servicehost_cluster_name varchar(128),
    teams_chat_id            varchar(128),
    yt_cluster               varchar(32),
    toggles_online_tenant    varchar(128),
    toggles_offline_tenant   varchar(128),
    links                    jsonb not null default '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS registry.service_general_info
(
    uid             uuid primary key,
    service_uid     uuid not null unique references registry.service (uid) on delete cascade,
    pms_application varchar(512),
    hermes_group    varchar(128),
    links           jsonb not null default '{}'::jsonb
);

CREATE TABLE IF NOT EXISTS registry.service_snapshot_info
(
    uid           uuid primary key,
    service_uid   uuid not null references registry.service (uid) on delete cascade,
    snapshot_name varchar(128) not null
);

CREATE UNIQUE INDEX IF NOT EXISTS service_snapshot_info_service_snapshot_uix
    ON registry.service_snapshot_info (service_uid, snapshot_name);

ALTER TABLE registry.project
    DROP COLUMN IF EXISTS config,
    DROP COLUMN IF EXISTS links;

ALTER TABLE registry.vertical
    DROP COLUMN IF EXISTS config,
    DROP COLUMN IF EXISTS links;

ALTER TABLE registry.service
    DROP COLUMN IF EXISTS links;

DROP TABLE IF EXISTS registry.hermes_project CASCADE;

ALTER TABLE registry.one_cloud_service_info
    RENAME TO service_onecloud_info;

CREATE TABLE IF NOT EXISTS registry.project_onecloud_info
(
    uid                   uuid primary key,
    project_uid           uuid not null unique references registry.project (uid) on delete cascade,
    cloud_namespace       varchar(64),
    production_root_queue varchar(512),
    testing_root_queue    varchar(512)
);

CREATE TABLE IF NOT EXISTS registry.vertical_onecloud_info
(
    uid                       uuid primary key,
    vertical_uid              uuid not null unique references registry.vertical (uid) on delete cascade,
    production_root_queue     varchar(512),
    testing_root_queue        varchar(512),
    i2i_production_root_queue varchar(512)
);

CREATE TABLE IF NOT EXISTS registry.onecloud_datacenter
(
    uid          uuid primary key,
    project_uid  uuid references registry.project (uid) on delete cascade,
    vertical_uid uuid references registry.vertical (uid) on delete cascade,
    service_uid  uuid references registry.service (uid) on delete cascade,
    dc           registry.datacenter_code,
    is_canary    boolean,
    is_testing   boolean
);

ALTER TABLE registry.service_onecloud_info
    RENAME COLUMN cloud_queue TO production_queue;

ALTER TABLE registry.service_onecloud_info
    ADD COLUMN IF NOT EXISTS cloud_service_id varchar(128),
    ADD COLUMN IF NOT EXISTS canary_queue varchar(512),
    ADD COLUMN IF NOT EXISTS testing_queue varchar(512),
    ADD COLUMN IF NOT EXISTS subqueues text[];

ALTER TABLE registry.service_onecloud_info
    DROP COLUMN IF EXISTS namespace,
    DROP COLUMN IF EXISTS service_prefix,
    DROP COLUMN IF EXISTS dc;

CREATE TABLE IF NOT EXISTS registry.onesecret_secret
(
    uid               uuid primary key,
    project_uid       uuid references registry.project (uid) on delete cascade,
    vertical_uid      uuid references registry.vertical (uid) on delete cascade,
    service_uid       uuid references registry.service (uid) on delete cascade,
    secret_id         varchar(128),
    testing_secret_id varchar(128)
);

ALTER TABLE registry.service_gateway_details
    DROP COLUMN IF EXISTS hermes_group,
    DROP COLUMN IF EXISTS snapshots;

ALTER TABLE registry.service_base_details
    DROP COLUMN IF EXISTS hermes_group,
    DROP COLUMN IF EXISTS snapshots;

ALTER TABLE registry.service_meta_details
    DROP COLUMN IF EXISTS hermes_group,
    DROP COLUMN IF EXISTS snapshots;

ALTER TABLE registry.service_yt_proxy_details
    ADD COLUMN IF NOT EXISTS tenant varchar(128);

ALTER TABLE registry.service_yt_proxy_details
    DROP COLUMN IF EXISTS tenants,
    DROP COLUMN IF EXISTS handles;

CREATE TABLE IF NOT EXISTS registry.service_platform_gateway_details
(
    uid                uuid primary key,
    service_uid        uuid not null unique references registry.service (uid) on delete cascade,
    base_shards_amount integer
);

CREATE TABLE IF NOT EXISTS registry.service_mediator_details
(
    uid         uuid primary key,
    service_uid uuid not null unique references registry.service (uid) on delete cascade,
    with_cache  boolean
);

CREATE TABLE IF NOT EXISTS registry.service_factor_proxy_details
(
    uid         uuid primary key,
    service_uid uuid not null unique references registry.service (uid) on delete cascade
);

CREATE TABLE IF NOT EXISTS registry.service_selectors_details
(
    uid         uuid primary key,
    service_uid uuid not null unique references registry.service (uid) on delete cascade
);

CREATE TABLE IF NOT EXISTS registry.service_worker_details
(
    uid          uuid primary key,
    service_uid  uuid not null unique references registry.service (uid) on delete cascade,
    default_pool varchar(128)
);

CREATE TABLE IF NOT EXISTS registry.service_snapshots_builder_details
(
    uid         uuid primary key,
    service_uid uuid not null unique references registry.service (uid) on delete cascade
);

CREATE TABLE IF NOT EXISTS registry.service_meta_i2i_details
(
    uid         uuid primary key,
    service_uid uuid not null unique references registry.service (uid) on delete cascade
);

CREATE TABLE IF NOT EXISTS registry.service_base_i2i_details
(
    uid          uuid primary key,
    service_uid  uuid not null unique references registry.service (uid) on delete cascade,
    shards_count integer
);

CREATE TABLE IF NOT EXISTS registry.service_scheduler_i2i_details
(
    uid         uuid primary key,
    service_uid uuid not null unique references registry.service (uid) on delete cascade
);

CREATE TABLE IF NOT EXISTS registry.service_mongodb_info
(
    uid           uuid primary key,
    service_uid   uuid not null unique references registry.service (uid) on delete cascade,
    database_name varchar(128),
    user_name     varchar(128)
);

CREATE TABLE IF NOT EXISTS registry.teamcity_project
(
    uid                 uuid primary key,
    project_uid         uuid references registry.project (uid) on delete cascade,
    vertical_uid        uuid references registry.vertical (uid) on delete cascade,
    service_uid         uuid references registry.service (uid) on delete cascade,
    teamcity_project_id varchar(256)
);

ALTER TABLE registry.apptracer_project
    RENAME TO apptracer_project_info;

ALTER TABLE registry.apptracer_project_info
    ALTER COLUMN apptracer_project_id DROP NOT NULL;

ALTER TABLE registry.project
    DROP COLUMN IF EXISTS display_name;

ALTER TABLE registry.vertical
    DROP COLUMN IF EXISTS display_name;

ALTER TABLE registry.function
    DROP COLUMN IF EXISTS display_name;
