package ru.vk.recommender.sre.discoveryportalflow.persistence.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineDependencyInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineStageRunsDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.PipelineRunsPageResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageDependencyInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunDetailsResponse
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunStats
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.StageRunWithTasksInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.TaskDependencyInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.TaskLogInfo
import ru.vk.recommender.sre.discoveryportalflow.api.dto.flow.TaskRunInfo
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskLogEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.StageDependencyRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.TaskDependencyRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.TaskLogRepository
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry.StageUsageResolver
import java.time.Instant
import java.util.UUID

@Service
class FlowQueryService(
    private val pipelineRuntimeService: PipelineRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val taskRuntimeService: TaskRuntimeService,
    private val stageDependencyRepository: StageDependencyRepository,
    private val taskDependencyRepository: TaskDependencyRepository,
    private val taskLogRepository: TaskLogRepository,
    private val stageUsageResolver: StageUsageResolver,
    private val flowContextRuntimeService: FlowContextRuntimeService,
    private val objectMapper: ObjectMapper,
) {

    fun getPipelineRuns(page: Int): PipelineRunsPageResponse {
        require(page > 0) { "Page must be greater than 0" }
        val pageSize = PIPELINE_RUNS_PAGE_SIZE
        val totalItems = pipelineRuntimeService.countRootPipelineRuns()
        val totalPages = if (totalItems == 0L) 0 else ((totalItems + pageSize - 1) / pageSize).toInt()
        val offset = (page - 1L) * pageSize
        val pipelineRuns = pipelineRuntimeService
            .getRootPipelineRunsPage(limit = pageSize, offset = offset)
            .map(::pipelineRunInfo)

        return PipelineRunsPageResponse(
            page = page,
            pageSize = pageSize,
            totalItems = totalItems,
            totalPages = totalPages,
            pipelineRuns = pipelineRuns,
        )
    }

    fun getPipelineRunDetails(pipelineRunId: UUID): PipelineRunDetailsResponse {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        return pipelineRunDetails(pipelineRun)
    }

    fun getPipelineContext(pipelineRunId: UUID): ObjectNode {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        return readPipelineContext(pipelineRun)
    }

    fun updatePipelineContext(pipelineRunId: UUID, pipelineContext: ObjectNode): ObjectNode {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        flowContextRuntimeService.saveFlowContext(pipelineRun.flowContextId, pipelineContext)
        return pipelineContext
    }

    fun getStageRunDetails(stageRunId: UUID): StageRunDetailsResponse {
        val stageRun = stageRuntimeService.getStageRun(stageRunId)
        val taskRuns = taskRuntimeService.getTaskRunsForStageRun(stageRun)
        val taskRunIds = taskRuns.mapNotNull { taskRun -> taskRun.id }
        val dependencies = taskRunIds.takeIf { taskRunIds.isNotEmpty() }
            ?.let(taskDependencyRepository::findByTaskRunIdIn)
            ?: emptyList()
        val taskNameById = taskRuns.associate { taskRun -> taskRun.requireId() to taskRun.taskName }
        val taskDefinitionsByName = taskDefinitionsByName(stageRun)
        return StageRunDetailsResponse(
            stageRun = stageRunInfo(stageRun),
            parentPipelineRunId = stageRun.requirePipelineRunId(),
            parentPipelineName = stageRun.requirePipelineName(),
            stats = stageRunStats(taskRuns),
            tasks = taskRuns.map { taskRun -> taskRunInfo(taskRun, taskDefinitionsByName) },
            dependencies = dependencies.map { dependency -> taskDependencyInfo(dependency, taskNameById) },
        )
    }

    fun getPipelineStageRunsDetails(pipelineRunId: UUID): PipelineStageRunsDetailsResponse {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        require(pipelineRun.childrenType == PipelineChildrenType.STAGE) {
            "Pipeline '${pipelineRun.pipelineName}' must have childrenType=STAGE, but was ${pipelineRun.childrenType}"
        }

        val stages = stageRuntimeService.getStageRunsForPipelineRun(pipelineRun.requireId())
        val stageRunIds = stages.map { stageRun -> stageRun.requireId() }
        val taskRuns = taskRuntimeService.getTaskRunsForStageRunIds(stageRunIds)
        val taskRunsByStageRunId = taskRuns.groupBy { taskRun -> taskRun.stageRunId }
        val taskRunIds = taskRuns.mapNotNull { taskRun -> taskRun.id }
        val taskDependencies = taskRunIds.takeIf { ids -> ids.isNotEmpty() }
            ?.let(taskDependencyRepository::findByTaskRunIdIn)
            ?: emptyList()
        val taskDependenciesByTaskRunId = taskDependencies.groupBy { dependency -> dependency.taskRunId }
        val taskNameById = taskRuns.associate { taskRun -> taskRun.requireId() to taskRun.taskName }
        val stageNameById = stages.associate { stageRun -> stageRun.requireId() to stageRun.requireStageName() }
        val stageDependencies = stageRunIds.takeIf { ids -> ids.isNotEmpty() }
            ?.let(stageDependencyRepository::findByStageRunIdIn)
            ?: emptyList()

        return PipelineStageRunsDetailsResponse(
            pipelineRun = pipelineRunInfo(pipelineRun),
            stages = stages.map { stageRun ->
                val stageTaskRuns = taskRunsByStageRunId[stageRun.requireId()] ?: emptyList()
                val taskDefinitionsByName = taskDefinitionsByName(stageRun)
                StageRunWithTasksInfo(
                    stageRun = stageRunInfo(stageRun),
                    stats = stageRunStats(stageTaskRuns),
                    tasks = stageTaskRuns.map { taskRun -> taskRunInfo(taskRun, taskDefinitionsByName) },
                    dependencies = stageTaskRuns.flatMap { taskRun ->
                        taskDependenciesByTaskRunId[taskRun.requireId()] ?: emptyList()
                    }.map { dependency -> taskDependencyInfo(dependency, taskNameById) },
                )
            },
            stageDependencies = stageDependencies.map { dependency -> stageDependencyInfo(dependency, stageNameById) },
        )
    }

    fun getTaskLogs(taskRunId: UUID): List<TaskLogInfo> {
        taskRuntimeService.getTaskRun(taskRunId)
        return taskLogRepository.findByTaskRunIdOrderByCreatedAt(taskRunId).map(::taskLogInfo)
    }

    private fun pipelineRunDetails(pipelineRun: PipelineRunEntity): PipelineRunDetailsResponse {
        val parentPipelineName = pipelineRun.parentPipelineRunId
            ?.let { parentId -> pipelineRuntimeService.getPipelineRun(parentId).pipelineName }

        val childPipelineRuns = pipelineRuntimeService.getChildPipelineRuns(pipelineRun.requireId())
        val childPipelines = childPipelineRuns.map(::pipelineRunInfo)
        val childPipelineNameById = childPipelineRuns.associate { childPipelineRun ->
            childPipelineRun.requireId() to childPipelineRun.pipelineName
        }
        val pipelineDependencies = childPipelineRuns.mapNotNull { childPipelineRun -> childPipelineRun.id }
            .takeIf { childPipelineRunIds -> childPipelineRunIds.isNotEmpty() }
            ?.let(pipelineRuntimeService::getDependenciesForPipelineRuns)
            ?: emptyList()

        val stages = stageRuntimeService.getStageRunsForPipelineRun(pipelineRun.requireId())
        val stageNameById = stages.associate { stageRun -> stageRun.requireId() to stageRun.requireStageName() }
        val stageDependencies = stages.mapNotNull { stageRun -> stageRun.id }
            .takeIf { stageRunIds -> stageRunIds.isNotEmpty() }
            ?.let(stageDependencyRepository::findByStageRunIdIn)
            ?: emptyList()

        return PipelineRunDetailsResponse(
            pipelineRun = pipelineRunInfo(pipelineRun),
            parentPipelineName = parentPipelineName,
            childPipelines = childPipelines,
            pipelineDependencies = pipelineDependencies.map { dependency ->
                pipelineDependencyInfo(dependency, childPipelineNameById)
            },
            stages = stages.map(::stageRunInfo),
            stageDependencies = stageDependencies.map { dependency ->
                stageDependencyInfo(dependency, stageNameById)
            },
        )
    }

    private fun pipelineRunInfo(pipelineRun: PipelineRunEntity): PipelineRunInfo {
        val childrenProgress = pipelineChildrenProgress(pipelineRun)
        return PipelineRunInfo(
            id = pipelineRun.requireId(),
            pipelineName = pipelineRun.pipelineName,
            childrenType = pipelineRun.childrenType,
            parentPipelineRunId = pipelineRun.parentPipelineRunId,
            status = pipelineRun.status,
            totalChildren = childrenProgress.totalChildren,
            completedChildren = childrenProgress.completedChildren,
            createdAt = Instant.ofEpochSecond(pipelineRun.createdAt),
            startedAt = epochSecondsToInstant(pipelineRun.startedAt),
            finishedAt = epochSecondsToInstant(pipelineRun.finishedAt),
            summary = parseSummary(pipelineRun.summary),
        )
    }

    private fun stageRunInfo(stageRun: StageRunEntity): StageRunInfo {
        return StageRunInfo(
            id = stageRun.requireId(),
            pipelineRunId = stageRun.requirePipelineRunId(),
            pipelineName = stageRun.requirePipelineName(),
            stageName = stageRun.requireStageName(),
            status = stageRun.status,
            startedAt = epochSecondsToInstant(stageRun.startedAt),
            finishedAt = epochSecondsToInstant(stageRun.finishedAt),
            summary = parseSummary(stageRun.summary),
        )
    }

    private fun stageRunStats(taskRuns: List<TaskRunEntity>): StageRunStats {
        val counts = taskRuns.groupingBy { taskRun -> taskRun.status }.eachCount()
        return StageRunStats(
            total = taskRuns.size,
            pending = counts[FlowStatus.PENDING] ?: 0,
            ready = counts[FlowStatus.READY] ?: 0,
            running = counts[FlowStatus.RUNNING] ?: 0,
            waiting = counts[FlowStatus.WAITING] ?: 0,
            blocked = counts[FlowStatus.BLOCKED] ?: 0,
            succeeded = counts[FlowStatus.SUCCEEDED] ?: 0,
            failed = counts[FlowStatus.FAILED] ?: 0,
            skipped = counts[FlowStatus.SKIPPED] ?: 0,
            canceled = counts[FlowStatus.CANCELED] ?: 0,
        )
    }

    private fun taskRunInfo(
        taskRun: TaskRunEntity,
        taskDefinitionsByName: Map<String, TaskDefinitionProperties>,
    ): TaskRunInfo {
        val taskDefinition = taskDefinitionsByName[taskRun.taskName]
            ?: error("No task definition found for task '${taskRun.taskName}'")
        return TaskRunInfo(
            id = taskRun.requireId(),
            taskName = taskRun.taskName,
            status = taskRun.status,
            startedAt = epochSecondsToInstant(taskRun.startedAt),
            finishedAt = epochSecondsToInstant(taskRun.finishedAt),
            attemptNumber = taskRun.attemptNumber,
            currentAttempt = taskRun.attemptNumber + 1,
            totalAttempts = taskDefinition.retryPolicy.attempts,
        )
    }

    private fun taskDefinitionsByName(stageRun: StageRunEntity): Map<String, TaskDefinitionProperties> {
        return stageUsageResolver.getTaskDefinitions(
            pipelineName = stageRun.requirePipelineName(),
            stageName = stageRun.requireStageName(),
        ).associateBy { definition -> definition.taskName }
    }

    private fun pipelineChildrenProgress(pipelineRun: PipelineRunEntity): PipelineChildrenProgress {
        val childStatuses = when (pipelineRun.childrenType) {
            PipelineChildrenType.PIPELINE -> pipelineRuntimeService.getChildPipelineRuns(pipelineRun.requireId())
                .map { childPipelineRun -> childPipelineRun.status }

            PipelineChildrenType.STAGE -> stageRuntimeService.getStageRunsForPipelineRun(pipelineRun.requireId())
                .map { stageRun -> stageRun.status }
        }

        return PipelineChildrenProgress(
            totalChildren = childStatuses.size,
            completedChildren = childStatuses.count { status -> status in completedChildStatuses },
        )
    }

    private fun taskDependencyInfo(
        dependency: TaskDependencyEntity,
        taskNameById: Map<UUID, String>,
    ): TaskDependencyInfo {
        val taskName = requireNotNull(taskNameById[dependency.taskRunId]) {
            "Task run name is required for dependency ${dependency.taskRunId}"
        }
        val dependencyTaskName = requireNotNull(taskNameById[dependency.dependencyTaskRunId]) {
            "Task run name is required for dependency ${dependency.dependencyTaskRunId}"
        }
        return TaskDependencyInfo(
            taskRunId = dependency.taskRunId,
            dependencyTaskRunId = dependency.dependencyTaskRunId,
            taskName = taskName,
            dependencyTaskName = dependencyTaskName,
        )
    }

    private fun stageDependencyInfo(
        dependency: StageDependencyEntity,
        stageNameById: Map<UUID, String>,
    ): StageDependencyInfo {
        val stageName = requireNotNull(stageNameById[dependency.stageRunId]) {
            "Stage run name is required for dependency ${dependency.stageRunId}"
        }
        val dependencyStageName = requireNotNull(stageNameById[dependency.dependencyStageRunId]) {
            "Stage run name is required for dependency ${dependency.dependencyStageRunId}"
        }
        return StageDependencyInfo(
            stageRunId = dependency.stageRunId,
            dependencyStageRunId = dependency.dependencyStageRunId,
            stageName = stageName,
            dependencyStageName = dependencyStageName,
        )
    }

    private fun pipelineDependencyInfo(
        dependency: PipelineDependencyEntity,
        pipelineNameById: Map<UUID, String>,
    ): PipelineDependencyInfo {
        val pipelineName = requireNotNull(pipelineNameById[dependency.pipelineRunId]) {
            "Pipeline run name is required for dependency ${dependency.pipelineRunId}"
        }
        val dependencyPipelineName = requireNotNull(pipelineNameById[dependency.dependencyPipelineRunId]) {
            "Pipeline run name is required for dependency ${dependency.dependencyPipelineRunId}"
        }
        return PipelineDependencyInfo(
            pipelineRunId = dependency.pipelineRunId,
            dependencyPipelineRunId = dependency.dependencyPipelineRunId,
            pipelineName = pipelineName,
            dependencyPipelineName = dependencyPipelineName,
        )
    }

    private fun taskLogInfo(taskLog: TaskLogEntity): TaskLogInfo {
        return TaskLogInfo(
            id = taskLog.requireId(),
            status = taskLog.status,
            type = taskLog.type,
            message = taskLog.message,
            createdAt = Instant.ofEpochSecond(taskLog.createdAt),
        )
    }

    private fun parseSummary(summary: String): JsonNode {
        return runCatching { objectMapper.readTree(summary) }
            .getOrElse { objectMapper.createObjectNode() }
    }

    private fun readPipelineContext(pipelineRun: PipelineRunEntity): ObjectNode {
        val flowContext = flowContextRuntimeService.getFlowContext(pipelineRun.flowContextId)
        val pipelineContext = objectMapper.readTree(flowContext.context)
        require(pipelineContext is ObjectNode) { "Pipeline context must be a JSON object" }
        return pipelineContext
    }

    private fun epochSecondsToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochSecond)

    private data class PipelineChildrenProgress(
        val totalChildren: Int,
        val completedChildren: Int,
    )

    private companion object {
        const val PIPELINE_RUNS_PAGE_SIZE = 10

        val completedChildStatuses = setOf(
            FlowStatus.SUCCEEDED,
            FlowStatus.SKIPPED,
        )
    }
}
