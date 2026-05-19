package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.JsonPathWriter

@Component
class DcSettingsInputMapper(
    jsonPathWriter: JsonPathWriter,
) : PrefixedObjectInputMapper(
    sourcePrefix = "dcSettings",
    targetPath = "dcSettings",
    jsonPathWriter = jsonPathWriter,
)
