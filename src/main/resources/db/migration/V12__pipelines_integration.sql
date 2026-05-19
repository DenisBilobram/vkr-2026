DO $$
BEGIN
    ALTER TYPE status ADD VALUE IF NOT EXISTS 'BLOCKED';
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS flow_context (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    context JSONB NOT NULL DEFAULT '{}'::jsonb
);

INSERT INTO flow_context (id, context)
SELECT id, context
FROM stage_context
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS pipeline_run (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    flow_context_id        UUID NOT NULL,
    pipeline_name          VARCHAR(128) NOT NULL,
    children_type          VARCHAR(16) NOT NULL,
    parent_pipeline_run_id UUID,
    status                 status NOT NULL DEFAULT 'PENDING',
    created_at             BIGINT NOT NULL DEFAULT CAST(EXTRACT(EPOCH FROM clock_timestamp()) AS BIGINT),
    started_at             BIGINT,
    finished_at            BIGINT,
    summary                JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX IF NOT EXISTS idx_pipeline_run_parent_pipeline_run_id
    ON pipeline_run(parent_pipeline_run_id);

CREATE INDEX IF NOT EXISTS idx_pipeline_run_status
    ON pipeline_run(status);

CREATE INDEX IF NOT EXISTS idx_pipeline_run_pipeline_name
    ON pipeline_run(pipeline_name);

CREATE INDEX IF NOT EXISTS idx_pipeline_run_flow_context_id
    ON pipeline_run(flow_context_id);

ALTER TABLE stage_run
    ADD COLUMN IF NOT EXISTS pipeline_run_id UUID;

ALTER TABLE stage_run
    ADD COLUMN IF NOT EXISTS pipeline_name VARCHAR(128);

ALTER TABLE stage_run
    ADD COLUMN IF NOT EXISTS flow_context_id UUID;

UPDATE stage_run
SET flow_context_id = stage_context_id
WHERE flow_context_id IS NULL
  AND stage_context_id IS NOT NULL;

UPDATE stage_run AS sr
SET pipeline_name = pr.pipeline_name
FROM pipeline_run AS pr
WHERE sr.pipeline_run_id = pr.id
  AND sr.pipeline_name IS NULL;

UPDATE stage_run AS sr
SET flow_context_id = pr.flow_context_id
FROM pipeline_run AS pr
WHERE sr.pipeline_run_id = pr.id
  AND sr.flow_context_id IS NULL
  AND pr.flow_context_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_stage_run_pipeline_run_id
    ON stage_run(pipeline_run_id);

CREATE INDEX IF NOT EXISTS idx_stage_run_pipeline_name
    ON stage_run(pipeline_name);

CREATE INDEX IF NOT EXISTS idx_stage_run_flow_context_id
    ON stage_run(flow_context_id);

CREATE TABLE IF NOT EXISTS stage_dependency (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_run_id            UUID NOT NULL,
    dependency_stage_run_id UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_stage_dependency_stage_run_id
    ON stage_dependency(stage_run_id);

CREATE INDEX IF NOT EXISTS idx_stage_dependency_dependency_stage_run_id
    ON stage_dependency(dependency_stage_run_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_stage_dependency_unique
    ON stage_dependency(stage_run_id, dependency_stage_run_id);
