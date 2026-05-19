package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder

import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

object OnecloudServiceManifestBuilder {

    fun buildRecommenderEnvironment(recommenderRuntime: RecommenderRuntime): Map<String, String> {
        return mapOf("VERTICAL" to recommenderRuntime.vertical)
    }

    fun buildPlatformEnvironment(serviceRuntime: ServiceRuntime): Map<String, String> {
        return mapOf(
            "PLATFORM_PRODUCT" to serviceRuntime.namespace,
            "SECRETS_ENV" to serviceRuntime.namespace,
            "VERTICAL" to serviceRuntime.namespace.replace('-', '_'),
        )
    }

    fun buildServiceManifestSpec(
        productId: Int?,
        serviceRuntime: ServiceRuntime,
        defaultCloudPods: Int,
        createMinOneCloudConfiguration: Boolean,
        cloudCpuVcores: Int,
        cloudRamG: Int,
        cloudLanOut: String,
        cloudLanIn: String,
        cloudVolumeSize: String,
        cloudAvailability: String,
        cloudJavaXms: String,
        cloudJavaXmx: String,
        imageName: String = serviceRuntime.cloudServiceName,
        imageVersion: String = "stable",
        cloudVolumeType: String = "nvme",
        additionalEnv: Map<String, String> = emptyMap(),
        additionalTestingEnv: Map<String, String> = emptyMap(),
        extraCanaryEnv: Map<String, String> = emptyMap(),
        enableApptracer: Boolean = true,
        wanNetworks: List<String> = listOf("v6"),
        wlanNetworks: List<String> = listOf("v6"),
        ports: List<String> = listOf("81", "82", "24816"),
        extraPorts: List<String> = emptyList(),
        maxTestingCloudPods: Int = 2,
        maxCanaryCloudPods: Int = 1000,
        maxTestingCloudCpuVcores: Int = 4,
        testingCloudLanOut: String = "30M",
        testingCloudLanIn: String = "30M",
        cloudPause: String = "1m",
        customCanaryAvailability: String? = null,
        serviceWithSnapshot: Boolean = true,
        startAttemptsLimit: Int? = null,
        enablePrometheus: Boolean = true,
        enableServiceHost: Boolean = true,
        pingPort: Int = 82,
        javaMainClass: String = "org.springframework.boot.loader.JarLauncher",
        zenApplicationName: String? = null,
    ): ServiceManifestSpec {
        return ServiceManifestSpec(
            cloudPods = if (createMinOneCloudConfiguration) 1 else defaultCloudPods,
            cloudCpuVcores = cloudCpuVcores,
            cloudRam = cloudRamG,
            cloudLanOut = cloudLanOut,
            cloudLanIn = cloudLanIn,
            cloudVolumeSize = cloudVolumeSize,
            cloudVolumeType = cloudVolumeType,
            cloudAvailability = cloudAvailability,
            cloudJavaXms = cloudJavaXms,
            cloudJavaXmx = cloudJavaXmx,
            imageName = imageName,
            imageVersion = imageVersion,
            additionalEnv = additionalEnv,
            additionalTestingEnv = additionalTestingEnv,
            extraCanaryEnv = extraCanaryEnv,
            enableApptracer = enableApptracer,
            wanNetworks = wanNetworks,
            wlanNetworks = wlanNetworks,
            ports = ports,
            extraPorts = extraPorts,
            maxTestingCloudPods = maxTestingCloudPods,
            maxCanaryCloudPods = maxCanaryCloudPods,
            maxTestingCloudCpuVcores = maxTestingCloudCpuVcores,
            testingCloudLanOut = testingCloudLanOut,
            testingCloudLanIn = testingCloudLanIn,
            cloudPause = cloudPause,
            customCanaryAvailability = customCanaryAvailability,
            serviceWithSnapshot = serviceWithSnapshot,
            startAttemptsLimit = startAttemptsLimit,
            enablePrometheus = enablePrometheus,
            enableServiceHost = enableServiceHost,
            enableHermesSidecar = serviceRuntime.hasSnapshots,
            enableVectorSidecar = serviceRuntime.hasVector,
            pingPort = pingPort,
            javaMainClass = javaMainClass,
            zenApplicationName = zenApplicationName,
            productId = productId,
        )
    }
}
