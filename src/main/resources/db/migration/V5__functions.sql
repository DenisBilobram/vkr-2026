create table if not exists registry.function
(
    uid          uuid primary key,
    vertical_uid uuid not null references registry.vertical (uid) on delete cascade,

    server_name  varchar(64) not null, -- vk-video-d2d, vk-video-continue-watching
    display_name varchar(64) not null  -- VK Video D2D, VK Video Continue Watching
);

create unique index if not exists function_server_name_uq
    on registry.function (server_name);

create table if not exists registry.function_config
(
    uid           uuid primary key,
    function_uid  uuid not null references registry.function (uid) on delete cascade,
    service_type  registry.service_type not null,

    config        jsonb not null default '{}'::jsonb,

    unique (function_uid, service_type)
);
