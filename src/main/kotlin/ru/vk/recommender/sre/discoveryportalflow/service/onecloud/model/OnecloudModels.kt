package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model

import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.RecommenderDcSettings
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment

data class OnecloudEnvironmentSetup(
    val environment: ServiceEnvironment,
    val datacenterCode: String,
    val queueId: String,
)

data class OnecloudEnvironmentResources(
    val cloudReplicas: Int,
    val cloudAvailability: String,
    val cloudLanOut: String,
    val cloudLanIn: String,
    val allocatedCpuCores: Int,
    val cloudPause: String?,
)

data class ServiceManifestSpec(
    val cloudPods: Int,
    val cloudCpuVcores: Int,
    val cloudRam: Int,
    val cloudLanOut: String,
    val cloudLanIn: String,
    val cloudVolumeSize: String,
    val cloudVolumeType: String,
    val cloudAvailability: String,
    val cloudJavaXms: String,
    val cloudJavaXmx: String,
    val imageName: String,
    val imageVersion: String,
    val additionalEnv: Map<String, String> = emptyMap(),
    val additionalTestingEnv: Map<String, String> = emptyMap(),
    val extraCanaryEnv: Map<String, String> = emptyMap(),
    val enableApptracer: Boolean = true,
    val wanNetworks: List<String> = listOf("v6"),
    val wlanNetworks: List<String> = listOf("v6"),
    val ports: List<String> = listOf("81", "82", "24816"),
    val extraPorts: List<String> = emptyList(),
    val maxTestingCloudPods: Int = 2,
    val maxCanaryCloudPods: Int = 1000,
    val maxTestingCloudCpuVcores: Int = 4,
    val testingCloudLanOut: String = "30M",
    val testingCloudLanIn: String = "30M",
    val cloudPause: String = "1m",
    val customCanaryAvailability: String? = null,
    val serviceWithSnapshot: Boolean = true,
    val startAttemptsLimit: Int? = null,
    val enablePrometheus: Boolean = true,
    val enableHermesSidecar: Boolean = false,
    val enableVectorSidecar: Boolean = false,
    val enableServiceHost: Boolean = true,
    val pingPort: Int = 82,
    val javaMainClass: String = "org.springframework.boot.loader.JarLauncher",
    val zenApplicationName: String? = null,
    val productId: Int? = null,
)

internal fun RecommenderDcSettings.uniqueProductionDatacenters(): List<String> {
    return (productionDcs + canaryDcs).distinct().sorted()
}
