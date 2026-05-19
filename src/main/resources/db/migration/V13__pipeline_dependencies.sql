CREATE TABLE IF NOT EXISTS pipeline_dependency (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pipeline_run_id            UUID NOT NULL,
    dependency_pipeline_run_id UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pipeline_dependency_pipeline_run_id
    ON pipeline_dependency(pipeline_run_id);

CREATE INDEX IF NOT EXISTS idx_pipeline_dependency_dependency_pipeline_run_id
    ON pipeline_dependency(dependency_pipeline_run_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_pipeline_dependency_unique
    ON pipeline_dependency(pipeline_run_id, dependency_pipeline_run_id);
