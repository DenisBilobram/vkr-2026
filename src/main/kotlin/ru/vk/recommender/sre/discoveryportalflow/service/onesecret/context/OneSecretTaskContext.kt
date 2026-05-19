package ru.vk.recommender.sre.discoveryportalflow.service.onesecret.context

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.OneSecretQueueTarget
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.RedisInfo
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretTargetPlan
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.YtInfo
import java.nio.file.Path

@JsonIgnoreProperties(ignoreUnknown = true)
data class OneSecretTaskContext(
    val workspaceRoot: Path,
    val recommenderName: String,
    val productId: Int? = null,
    @JsonAlias("parentProductName")
    val projectName: String? = null,
    @JsonAlias("parentProductId")
    val projectProductId: Int? = null,
    val apptracerToken: String? = null,
    var recomOneSecretId: String? = null,
    var projectOneSecretId: String? = null,
    val ytOffline: YtInfo,
    val redis: RedisInfo,
    val verticalQueueTargets: List<OneSecretQueueTarget> = emptyList(),
    val projectQueueTargets: List<OneSecretQueueTarget> = emptyList(),
    val serviceTargets: List<ServiceOneSecretTargetPlan> = emptyList(),
    val serviceOneSecretOutcomes: MutableMap<String, ServiceOneSecretOutcome> = linkedMapOf(),
) : FlowTaskContext
