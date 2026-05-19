package ru.vk.recommender.sre.discoveryportalflow.persistence.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowContextEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.repository.FlowContextRepository
import java.util.UUID

@Service
class FlowContextRuntimeService(
    private val flowContextRepository: FlowContextRepository,
) {

    fun createFlowContext(contextJson: JsonNode): UUID {
        val saved = flowContextRepository.save(FlowContextEntity(context = contextJson.toString()))
        return saved.requireId()
    }

    fun getFlowContext(flowContextId: UUID): FlowContextEntity {
        return flowContextRepository.findByIdOrNull(flowContextId)
            ?: error("Can't find flow context with id=$flowContextId")
    }

    fun saveFlowContext(flowContextId: UUID, contextJson: JsonNode): FlowContextEntity {
        return flowContextRepository.save(
            FlowContextEntity(
                id = flowContextId,
                context = contextJson.toString(),
            )
        )
    }
}
