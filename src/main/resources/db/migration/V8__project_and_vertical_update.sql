begin;

alter table registry.project
    add column if not exists config jsonb not null default '{}'::jsonb,
    add column if not exists links jsonb not null default '{}'::jsonb;

alter table registry.project
    drop constraint if exists project_abc_id_format_chk,
    drop constraint if exists project_weight_range_chk;

alter table registry.project
    drop constraint if exists project_abc_id_key,
    drop constraint if exists project_prm_key;

alter table registry.project
    drop column if exists abc_id,
    drop column if exists prm,
    drop column if exists weight;

alter table registry.vertical
    add column if not exists config jsonb not null default '{}'::jsonb,
    add column if not exists links jsonb not null default '{}'::jsonb;

alter table registry.vertical
    drop constraint if exists vertical_abc_id_format_chk,
    drop constraint if exists vertical_weight_range_chk;

alter table registry.vertical
    drop column if exists abc_id,
    drop column if exists prm,
    drop column if exists weight;

commit;
