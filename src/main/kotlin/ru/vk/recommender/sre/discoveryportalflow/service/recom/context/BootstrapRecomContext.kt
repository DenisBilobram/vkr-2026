package ru.vk.recommender.sre.discoveryportalflow.service.recom.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.ServiceOneSecretOutcome
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.OneSecretSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtCluster

@JsonIgnoreProperties(ignoreUnknown = true)
data class BootstrapRecomContext(
    val recommender: RecommenderConfig,
    val services: List<RecommenderServiceConfig> = emptyList(),
    val dcSettings: RecommenderDcSettings = RecommenderDcSettings(),
    val servicehostClusterName: String? = null,
    val oneSecret: OneSecretSettings = OneSecretSettings(),
    val apptracerToken: String? = oneSecret.apptracerToken,
    val recomOneSecretId: String? = null,
    val projectOneSecretId: String? = null,
    val serviceOneSecretOutcomes: Map<String, ServiceOneSecretOutcome> = emptyMap(),
    val branch: String? = null,
    val teamsChatId: String? = null,
    val fishJiraTask: String = "",
    val ytCluster: YtCluster = YtCluster.JUPITER,
) : FlowTaskContext
