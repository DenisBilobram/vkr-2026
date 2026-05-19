package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import java.util.UUID

interface PipelineRunRepository : CrudRepository<PipelineRunEntity, UUID> {

    fun findByParentPipelineRunId(parentPipelineRunId: UUID): List<PipelineRunEntity>

    fun findByStatus(status: FlowStatus): List<PipelineRunEntity>

    @Query(
        """
        SELECT *
        FROM pipeline_run
        WHERE parent_pipeline_run_id IS NULL
        ORDER BY created_at DESC, id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun findRootPipelineRunsPage(
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<PipelineRunEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM pipeline_run
        WHERE parent_pipeline_run_id IS NULL
        """
    )
    fun countRootPipelineRuns(): Long
}
