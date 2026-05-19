package ru.vk.recommender.sre.discoveryportalflow.api.runner.model

import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.model.api.OneSecretSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.FactorProxyServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MediatorServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MetaI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MetaServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.PlatformGatewayServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SelectorsServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SnapshotsBuilderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.WorkerServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.YtProxyServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.yt.model.YtCluster

typealias RecommenderPayload = RecommenderConfig
typealias ServicePayload = RecommenderServiceConfig
typealias BaseServicePayload = BaseServiceConfig
typealias MetaServicePayload = MetaServiceConfig
typealias MediatorServicePayload = MediatorServiceConfig
typealias BaseI2IServicePayload = BaseI2IServiceConfig
typealias MetaI2IServicePayload = MetaI2IServiceConfig
typealias SelectorsServicePayload = SelectorsServiceConfig
typealias YtProxyServicePayload = YtProxyServiceConfig
typealias FactorProxyServicePayload = FactorProxyServiceConfig
typealias PlatformGatewayServicePayload = PlatformGatewayServiceConfig
typealias WorkerServicePayload = WorkerServiceConfig
typealias SnapshotsBuilderServicePayload = SnapshotsBuilderServiceConfig
typealias DcSettingsPayload = RecommenderDcSettings
typealias OneSecretPayload = OneSecretSettings
typealias YtMasterClusterPayload = YtCluster

data class StageTasksPayload(
    val recommender: RecommenderPayload,
    val services: List<ServicePayload>,
    val dcSettings: DcSettingsPayload,
    val servicehostClusterName: String,
    val ytCluster: YtMasterClusterPayload = YtCluster.JUPITER,
    val branch: String? = null,
    val teamsChatId: String? = null,
    val teamsUsersToAdd: List<String> = emptyList(),
    val fishJiraTask: String,
    val oneSecret: OneSecretPayload = OneSecretSettings(),
)
