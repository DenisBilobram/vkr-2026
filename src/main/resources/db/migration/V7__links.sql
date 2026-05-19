create table if not exists registry.vertical_link
(
    uid uuid primary key,
    vertical_uid uuid not null references registry.vertical (uid) on delete cascade,

    toggles_link varchar(512),
    release_chat_link varchar(512)
);

create table if not exists registry.service_link
(
    uid uuid primary key,
    service_uid uuid not null references registry.service (uid) on delete cascade,

    links jsonb not null default '{}'::jsonb
);
