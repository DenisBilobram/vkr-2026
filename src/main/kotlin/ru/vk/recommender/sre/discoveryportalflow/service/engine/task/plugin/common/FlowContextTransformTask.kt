package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

abstract class FlowContextTransformTask<SourceContext : FlowTaskContext>(
    contextClass: KClass<SourceContext>,
) : FlowTask<SourceContext>(contextClass) {

    final override fun projectUpdatedContext(taskRunContext: SourceContext, objectMapper: ObjectMapper): JsonNode {
        return objectMapper.valueToTree(transformContext(taskRunContext))
    }

    protected abstract fun transformContext(taskRunContext: SourceContext): FlowTaskContext
}
