package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.mapper

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.api.context.JsonPathWriter

@Component
class RecommenderInputMapper(
    jsonPathWriter: JsonPathWriter,
) : PrefixedObjectInputMapper(
    sourcePrefix = "recommender",
    targetPath = "recommender",
    jsonPathWriter = jsonPathWriter,
)
