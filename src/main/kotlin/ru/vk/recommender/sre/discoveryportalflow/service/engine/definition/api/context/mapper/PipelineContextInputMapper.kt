package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper

import com.fasterxml.jackson.databind.node.ObjectNode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.RawPipelineContext

interface PipelineContextInputMapper {

    fun map(
        rawContext: RawPipelineContext,
        target: ObjectNode,
    )
}
