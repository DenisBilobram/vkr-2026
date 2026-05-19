begin;

alter table registry.service
    add column if not exists links jsonb not null default '{}'::jsonb;

drop table if exists registry.service_link cascade;
drop table if exists registry.vertical_link cascade;

commit;
