package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.context

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.FlowContextRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.PipelineRuntimeService
import ru.vk.recommender.sre.discoveryportalflow.persistence.service.StageRuntimeService
import java.util.UUID

@Service
class FlowContextManager(
    private val flowContextRuntimeService: FlowContextRuntimeService,
    private val pipelineRuntimeService: PipelineRuntimeService,
    private val stageRuntimeService: StageRuntimeService,
    private val mapper: ObjectMapper,
) {

    fun loadFlowContextByPipelineRunId(pipelineRunId: UUID): ObjectNode {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        return readFlowContext(pipelineRun.flowContextId)
    }

    fun saveFlowContextByPipelineRunId(pipelineRunId: UUID, updatedContext: JsonNode) {
        val pipelineRun = pipelineRuntimeService.getPipelineRun(pipelineRunId)
        flowContextRuntimeService.saveFlowContext(pipelineRun.flowContextId, updatedContext)
    }

    fun loadFlowContextByStageRunId(stageRunId: UUID): ObjectNode {
        val stageRun = stageRuntimeService.getStageRun(stageRunId)
        return readFlowContext(stageRun.flowContextId)
    }

    fun saveFlowContextByStageRunId(stageRunId: UUID, updatedContext: JsonNode) {
        val stageRun = stageRuntimeService.getStageRun(stageRunId)
        flowContextRuntimeService.saveFlowContext(stageRun.flowContextId, updatedContext)
    }

    private fun readFlowContext(flowContextId: UUID): ObjectNode {
        val flowContext = flowContextRuntimeService.getFlowContext(flowContextId)
        return mapper.readTree(flowContext.context) as ObjectNode
    }
}
