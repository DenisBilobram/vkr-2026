package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper

import com.fasterxml.jackson.databind.node.ObjectNode
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.JsonPathWriter
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.RawPipelineContext

abstract class PrefixedObjectInputMapper(
    private val sourcePrefix: String,
    private val targetPath: String,
    private val jsonPathWriter: JsonPathWriter,
) : PipelineContextInputMapper {

    override fun map(rawContext: RawPipelineContext, target: ObjectNode) {
        rawContext.entriesWithPrefix(sourcePrefix)
            .forEach { entry ->
                val fieldPath = entry.path.removePrefix("$sourcePrefix.")
                jsonPathWriter.set(target, "$targetPath.$fieldPath", entry.value)
            }
    }
}
