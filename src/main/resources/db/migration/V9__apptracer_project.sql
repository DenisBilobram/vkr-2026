create table if not exists registry.apptracer_project
(
    uid          uuid primary key,

    apptracer_project_name varchar(64) not null unique,
    apptracer_project_id   varchar(64) not null unique,

    project_uid  uuid references registry.project (uid) on delete cascade,
    vertical_uid uuid references registry.vertical (uid) on delete cascade,

    constraint cloud_service_owner_chk
        check (
            (project_uid is not null and vertical_uid is null)
                or
            (project_uid is null and vertical_uid is not null)
            )
);

-- не более одного apptracer_project на один project_uid
create unique index if not exists apptracer_project_one_per_project_uid_uix
    on registry.apptracer_project (project_uid)
    where project_uid is not null;

-- не более одного apptracer_project на одну vertical_uid
create unique index if not exists apptracer_project_one_per_vertical_uid_uix
    on registry.apptracer_project (vertical_uid)
    where vertical_uid is not null;
