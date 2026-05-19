-- Enums
DO $$
BEGIN
CREATE TYPE status AS ENUM ('PENDING', 'READY', 'RUNNING', 'WAITING', 'SUCCEEDED', 'FAILED', 'FAILED_WITH_RETRY', 'SKIPPED', 'CANCELED');
EXCEPTION
  WHEN duplicate_object THEN NULL;
END $$;

DO $$
BEGIN
CREATE TYPE log_type AS ENUM ('ERROR', 'WARN', 'INFO');
EXCEPTION
  WHEN duplicate_object THEN NULL;
END $$;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- pipeline_run
-- CREATE TABLE IF NOT EXISTS pipeline_run (
--     id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     status       status NOT NULL,
--     created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
--     started_at   TIMESTAMPTZ,
--     finished_at  TIMESTAMPTZ
--     );
--
-- CREATE INDEX IF NOT EXISTS idx_pipeline_run_status
--     ON pipeline_run(status);
--
-- stage_run
CREATE TABLE IF NOT EXISTS stage_run (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     pipeline_run_id UUID NOT NULL,
    stage_context_id  UUID,
    status            status NOT NULL,
    started_at        BIGINT,
    finished_at       BIGINT,
    summary           JSONB NOT NULL DEFAULT '{}'::jsonb
    );

-- CREATE INDEX IF NOT EXISTS idx_stage_run_pipeline_run_id
--     ON stage_run(pipeline_run_id);
--
CREATE INDEX IF NOT EXISTS idx_stage_run_status
    ON stage_run(status);

-- stage_context
CREATE TABLE IF NOT EXISTS stage_context (
     id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     context JSONB NOT NULL DEFAULT '{}'::jsonb
);

-- task_run
CREATE TABLE IF NOT EXISTS task_run (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_name          VARCHAR(40) NOT NULL,
    stage_run_id       UUID,
    status             status NOT NULL DEFAULT 'PENDING',
    started_at         BIGINT,
    finished_at        BIGINT,
    attempt_number     INTEGER NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_task_run_stage_run_id
    ON task_run(stage_run_id);

CREATE INDEX IF NOT EXISTS idx_task_run_status
    ON task_run(status);

CREATE INDEX IF NOT EXISTS idx_task_run_task_name
    ON task_run(task_name);

-- task_dependency
CREATE TABLE IF NOT EXISTS task_dependency (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_run_id              UUID NOT NULL,
    dependency_task_run_id   UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_task_dependency_task_run_id
    ON task_dependency(task_run_id);

CREATE INDEX IF NOT EXISTS idx_task_dependency_dependency_task_run_id
    ON task_dependency(dependency_task_run_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_task_dependency_unique
    ON task_dependency(task_run_id, dependency_task_run_id);

-- task_log
CREATE TABLE IF NOT EXISTS task_log (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_run_id    UUID NOT NULL,
    status         status,
    type           log_type NOT NULL,
    message        TEXT NOT NULL,
    created_at     BIGINT NOT NULL DEFAULT CAST(EXTRACT(EPOCH FROM clock_timestamp()) AS BIGINT)
    );

CREATE INDEX IF NOT EXISTS idx_task_log_task_run_id
    ON task_log(task_run_id);

CREATE INDEX IF NOT EXISTS idx_task_log_type
    ON task_log(type);

CREATE INDEX IF NOT EXISTS idx_task_log_created_at
    ON task_log(created_at);
