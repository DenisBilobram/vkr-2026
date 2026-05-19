package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.JsonPathWriter

@Component
class OneSecretInputMapper(
    jsonPathWriter: JsonPathWriter,
) : PrefixedObjectInputMapper(
    sourcePrefix = "oneSecret",
    targetPath = "oneSecret",
    jsonPathWriter = jsonPathWriter,
)
