create table if not exists registry.hermes_project
(
    uid                 uuid primary key,
    vertical_uid        uuid        not null unique references registry.vertical (uid) on delete cascade,

    hermes_project_name varchar(64) not null
);
