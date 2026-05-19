package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext

@JsonIgnoreProperties(ignoreUnknown = true)
data class OneSecretWriteTaskContext(
    val secretId: String,
    val secretData: Map<String, String>,
    val comment: String = "Updated by discovery-portal-flow",
) : FlowTaskContext
