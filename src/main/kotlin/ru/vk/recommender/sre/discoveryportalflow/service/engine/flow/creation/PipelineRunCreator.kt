package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.creation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.PipelineRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageDependencyEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.FlowContextRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.PipelineRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineStageReferenceProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.registry.PipelineDefinitionRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.pipeline.PipelineOrchestrator
import java.util.UUID

@Service
class PipelineRunCreator(
    private val pipelineDefinitionRegistry: PipelineDefinitionRegistry,
    private val flowContextRuntimeService: FlowContextRuntimeService,
    private val pipelineRuntimeService: PipelineRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val taskRunCreator: TaskRunCreator,
    private val pipelineOrchestrator: PipelineOrchestrator,
    private val objectMapper: ObjectMapper,
) {

    fun createPipelineRun(pipelineName: String, contextJson: JsonNode): PipelineRunEntity {
        val flowContextId = flowContextRuntimeService.createFlowContext(copyContext(contextJson))
        val pipelineRun = createPipelineRunTree(
            pipelineName = pipelineName,
            flowContextId = flowContextId,
            parentPipelineRunId = null,
        )
        pipelineOrchestrator.recomputePipelineSubtree(pipelineRun.requireId())
        return pipelineRuntimeService.getPipelineRun(pipelineRun.requireId())
    }

    private fun createPipelineRunTree(
        pipelineName: String,
        flowContextId: UUID,
        parentPipelineRunId: UUID?,
    ): PipelineRunEntity {
        val definition = pipelineDefinitionRegistry.getPipelineDefinition(pipelineName)
        val pipelineRun = pipelineRuntimeService.createPipelineRun(
            pipelineName = definition.pipelineName,
            childrenType = definition.childrenType,
            flowContextId = flowContextId,
            parentPipelineRunId = parentPipelineRunId,
        )

        when (definition.childrenType) {
            PipelineChildrenType.PIPELINE -> {
                val childPipelineRunsByName = definition.pipelines
                    .distinctBy { childPipelineDefinition -> childPipelineDefinition.pipelineName }
                    .associate { childPipelineDefinition ->
                        val childPipelineRun = createPipelineRunTree(
                            pipelineName = childPipelineDefinition.pipelineName,
                            flowContextId = flowContextId,
                            parentPipelineRunId = pipelineRun.requireId(),
                        )
                        childPipelineDefinition.pipelineName to childPipelineRun
                    }
                val dependencies = buildPipelineRunDependencies(definition.pipelines, childPipelineRunsByName)
                pipelineRuntimeService.savePipelineRunDependencies(dependencies)
            }

            PipelineChildrenType.STAGE -> {
                val stageRunsByName = definition.stages.associate { stageDefinition ->
                    val stageRun = stageRuntimeService.createPipelineStageRun(
                        pipelineRun = pipelineRun,
                        stageName = stageDefinition.stageName,
                    )
                    taskRunCreator.createTaskRuns(stageRun)
                    stageDefinition.stageName to stageRun
                }
                val dependencies = buildStageRunDependencies(definition.stages, stageRunsByName)
                stageRuntimeService.saveStageRunDependencies(dependencies)
            }
        }

        return pipelineRun
    }

    private fun buildPipelineRunDependencies(
        pipelineDefinitions: List<PipelineDefinitionProperties>,
        pipelineRunsByName: Map<String, PipelineRunEntity>,
    ): List<PipelineDependencyEntity> {
        fun pipelineRunIdByName(pipelineName: String): UUID = pipelineRunsByName.getValue(pipelineName).requireId()

        return pipelineDefinitions.flatMap { definition ->
            val pipelineRunId = pipelineRunIdByName(definition.pipelineName)
            definition.dependencyPipelines.distinct().map { dependencyPipelineName ->
                PipelineDependencyEntity(
                    pipelineRunId = pipelineRunId,
                    dependencyPipelineRunId = pipelineRunIdByName(dependencyPipelineName),
                )
            }
        }
    }

    private fun buildStageRunDependencies(
        stageDefinitions: List<PipelineStageReferenceProperties>,
        stageRunsByName: Map<String, StageRunEntity>,
    ): List<StageDependencyEntity> {
        fun stageRunIdByName(stageName: String): UUID = stageRunsByName.getValue(stageName).requireId()

        return stageDefinitions.flatMap { definition ->
            val stageRunId = stageRunIdByName(definition.stageName)
            definition.dependencyStages.distinct().map { dependencyStageName ->
                StageDependencyEntity(
                    stageRunId = stageRunId,
                    dependencyStageRunId = stageRunIdByName(dependencyStageName),
                )
            }
        }
    }

    private fun copyContext(contextJson: JsonNode): JsonNode {
        return objectMapper.readTree(contextJson.toString())
    }
}
