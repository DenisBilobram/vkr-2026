package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.RedisInfo
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.YtInfo

@JsonIgnoreProperties(ignoreUnknown = true)
data class OneSecretReadTaskContext(
    val secretId: String,
    val secretData: MutableMap<String, String> = linkedMapOf(),
    var apptracerToken: String? = null,
    var ytOffline: YtInfo = YtInfo(),
    var redis: RedisInfo = RedisInfo(),
) : FlowTaskContext
