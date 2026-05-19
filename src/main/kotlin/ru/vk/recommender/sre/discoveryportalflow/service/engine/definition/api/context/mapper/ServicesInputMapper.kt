package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.JsonPathWriter
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.RawPipelineContext

@Component
class ServicesInputMapper(
    private val objectMapper: ObjectMapper,
    private val jsonPathWriter: JsonPathWriter,
) : PipelineContextInputMapper {

    override fun map(rawContext: RawPipelineContext, target: ObjectNode) {
        val services = objectMapper.createArrayNode()

        rawContext.entriesWithPrefix(SERVICES_PREFIX)
            .groupBy { entry -> entry.path.removePrefix("$SERVICES_PREFIX.").substringBefore('.') }
            .forEach { (serviceType, entries) ->
                buildService(serviceType, entries.map { it.path to it.value })
                    ?.let(services::add)
            }

        if (services.size() > 0) {
            jsonPathWriter.set(target, SERVICES_PREFIX, services)
        }
    }

    private fun buildService(
        serviceType: String,
        entries: List<Pair<String, JsonNode>>,
    ): ObjectNode? {
        val servicePath = "$SERVICES_PREFIX.$serviceType"
        if (!hasSelectedServiceMarker(servicePath, entries) && !hasServiceFields(servicePath, entries)) {
            return null
        }

        val service = objectMapper.createObjectNode()
        service.put(TYPE_FIELD, serviceType)

        val servicePrefix = "$servicePath."
        entries.forEach { (path, value) ->
            val fieldPath = path.removePrefix(servicePrefix)
            if (fieldPath != path) {
                jsonPathWriter.set(service, fieldPath, value)
            }
        }

        return service
    }

    private fun hasSelectedServiceMarker(
        servicePath: String,
        entries: List<Pair<String, JsonNode>>,
    ): Boolean {
        return entries.any { (path, value) ->
            path == servicePath && (!value.isBoolean || value.asBoolean())
        }
    }

    private fun hasServiceFields(
        servicePath: String,
        entries: List<Pair<String, JsonNode>>,
    ): Boolean {
        val serviceFieldPrefix = "$servicePath."
        return entries.any { (path, _) -> path.startsWith(serviceFieldPrefix) }
    }

    private companion object {
        const val SERVICES_PREFIX = "services"
        const val TYPE_FIELD = "type"
    }
}
