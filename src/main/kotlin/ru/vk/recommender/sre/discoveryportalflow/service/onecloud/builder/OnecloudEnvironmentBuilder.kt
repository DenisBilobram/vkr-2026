package ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder

import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.OnecloudEnvironmentResources
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.model.ServiceManifestSpec
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

object OnecloudEnvironmentBuilder {

    fun buildEnvironmentVariables(
        serviceRuntime: ServiceRuntime,
        serviceManifestSpec: ServiceManifestSpec,
        environment: ServiceEnvironment,
        allocatedCpuCores: Int,
    ): Map<String, String> {
        val environmentVariables = linkedMapOf<String, String>()
        environmentVariables["PING_URL"] = "http://localhost:${serviceManifestSpec.pingPort}/ping"
        environmentVariables["JAVA_MAIN_CLASS"] = serviceManifestSpec.javaMainClass

        if (environment == ServiceEnvironment.TESTING) {
            environmentVariables += serviceManifestSpec.additionalTestingEnv
        } else {
            environmentVariables += serviceManifestSpec.additionalEnv
        }

        if (environment == ServiceEnvironment.CANARY) {
            environmentVariables += serviceManifestSpec.extraCanaryEnv
        }

        environmentVariables["ZEN_APPLICATION"] = serviceManifestSpec.zenApplicationName ?: serviceRuntime.cloudServiceName
        environmentVariables["YANDEX_ENVIRONMENT"] = when (environment) {
            ServiceEnvironment.PRODUCTION -> "production"
            ServiceEnvironment.CANARY -> "canary"
            ServiceEnvironment.TESTING -> "prestable"
        }
        environmentVariables["ZEN_ENVIRONMENT"] = when (environment) {
            ServiceEnvironment.PRODUCTION -> "production"
            ServiceEnvironment.CANARY -> "canary"
            ServiceEnvironment.TESTING -> "zeta"
        }
        environmentVariables["JAVA_XMS"] = serviceManifestSpec.cloudJavaXms
        environmentVariables["JAVA_XMX"] = serviceManifestSpec.cloudJavaXmx
        environmentVariables["CPU_CORES"] = allocatedCpuCores.toString()
        environmentVariables["cloud_tmpfs"] = "/tmpfs:1g"

        if (serviceManifestSpec.serviceWithSnapshot) {
            environmentVariables["WEB_GROUP"] = serviceRuntime.cloudServiceName
        }

        if ("v6" in serviceManifestSpec.wanNetworks || "v6" in serviceManifestSpec.wlanNetworks) {
            environmentVariables["USE_IPV6"] = "true"
        }

        if (environment == ServiceEnvironment.TESTING) {
            environmentVariables["ZETA_ENVIRONMENT"] = "default"
            environmentVariables["ZETA_ID"] = "default"
        } else if (serviceManifestSpec.serviceWithSnapshot) {
            environmentVariables["STAGE_TYPE"] = "PRODUCTION"
        }

        if (environment != ServiceEnvironment.TESTING && serviceManifestSpec.enableApptracer && !serviceManifestSpec.enableVectorSidecar) {
            environmentVariables["TRACER_VECTOR_ENABLED"] = "1"
        }

        if (serviceManifestSpec.enableServiceHost) {
            environmentVariables["mesh_version"] = "consul-dp-3"
            environmentVariables["mesh_dataplane_metrics_collector_port"] = "23569"
            environmentVariables["cloud_sd_tags"] = "mesh-cluster-public-prod"
            environmentVariables["mesh_dataplane_bootstrap_config_json"] =
                "{\"urls\":[\"https://mesh-operator.nda.example.invalid/api/v3/cloud/instance/dataplane-bootstrap\"]}"
            environmentVariables["prometheus_enabled"] = "true"
            environmentVariables["prometheus_use_ip"] = "wlan6"
            environmentVariables["prometheus_labels"] = "mesh_cluster=public-prod"
            environmentVariables["prometheus_metrics_cfg"] = if (serviceManifestSpec.enablePrometheus) {
                "/metrics:8083;/metrics:23569"
            } else {
                "/metrics:23569"
            }
        } else if (serviceManifestSpec.enablePrometheus) {
            environmentVariables["prometheus_enabled"] = "true"
            environmentVariables["prometheus_port"] = "8083"
            environmentVariables["prometheus_location"] = "/metrics"
            environmentVariables["prometheus_use_ip"] = "wlan6"
        }
        if (serviceManifestSpec.enableHermesSidecar) {
            environmentVariables["USE_SNAPSHOTS_SIDECAR"] = "true"
        }

        return environmentVariables
    }

    fun buildEnvironmentResources(
        environment: ServiceEnvironment,
        serviceManifestSpec: ServiceManifestSpec,
    ): OnecloudEnvironmentResources {
        val cloudReplicas = when (environment) {
            ServiceEnvironment.PRODUCTION -> serviceManifestSpec.cloudPods
            ServiceEnvironment.CANARY -> minOf(serviceManifestSpec.cloudPods, serviceManifestSpec.maxCanaryCloudPods)
            ServiceEnvironment.TESTING -> minOf(serviceManifestSpec.cloudPods, serviceManifestSpec.maxTestingCloudPods)
        }
        val cloudAvailability = when (environment) {
            ServiceEnvironment.PRODUCTION -> serviceManifestSpec.cloudAvailability
            ServiceEnvironment.CANARY -> serviceManifestSpec.customCanaryAvailability ?: serviceManifestSpec.cloudAvailability
            ServiceEnvironment.TESTING -> "1"
        }
        val cloudLanOut = if (environment == ServiceEnvironment.TESTING) {
            serviceManifestSpec.testingCloudLanOut
        } else {
            serviceManifestSpec.cloudLanOut
        }
        val cloudLanIn = if (environment == ServiceEnvironment.TESTING) {
            serviceManifestSpec.testingCloudLanIn
        } else {
            serviceManifestSpec.cloudLanIn
        }
        val allocatedCpuCores = if (environment == ServiceEnvironment.TESTING) {
            minOf(serviceManifestSpec.cloudCpuVcores, serviceManifestSpec.maxTestingCloudCpuVcores)
        } else {
            serviceManifestSpec.cloudCpuVcores
        }
        val cloudPause = serviceManifestSpec.cloudPause.takeUnless { environment == ServiceEnvironment.TESTING }

        return OnecloudEnvironmentResources(
            cloudReplicas = cloudReplicas,
            cloudAvailability = cloudAvailability,
            cloudLanOut = cloudLanOut,
            cloudLanIn = cloudLanIn,
            allocatedCpuCores = allocatedCpuCores,
            cloudPause = cloudPause,
        )
    }

    fun buildSidecars(
        serviceRuntime: ServiceRuntime,
        environment: ServiceEnvironment,
        serviceManifestSpec: ServiceManifestSpec
    ): List<Map<String, Any>> {
        val sidecars = mutableListOf<Map<String, Any>>()
        if (serviceManifestSpec.enableHermesSidecar) {
            sidecars += buildHermesSidecar(environment)
        }

        if (serviceManifestSpec.enableVectorSidecar) {
            when (environment) {
                ServiceEnvironment.PRODUCTION, ServiceEnvironment.CANARY -> {
                    sidecars += buildVectorSidecar(
                        recommenderName = serviceRuntime.namespace,
                        applicationName = serviceRuntime.cloudServiceName,
                        pmsAppName = serviceRuntime.pmsApplicationName,
                    )
                }

                else -> {}
            }
        }

        return sidecars
    }

    private fun buildHermesSidecar(
        environment: ServiceEnvironment,
        version: String = "0.master.4b27774fb690",
    ): Map<String, Any> {
        val env = mutableListOf(
            "ZEN_APPLICATION=hermes-sidecar",
            "YANDEX_ENVIRONMENT=${if (environment == ServiceEnvironment.TESTING) "testing" else "production"}",
            "ZEN_ENVIRONMENT=${if (environment == ServiceEnvironment.TESTING) "zeta" else "production"}",
            "CPU_CORES=1",
            "JAVA_XMS=50m",
            "JAVA_XMX=100m",
            "GOMEMLIMIT=100MiB",
            "JAVA_MAIN_CLASS=org.springframework.boot.loader.JarLauncher",
            "PING_URL=http://localhost:182/ping",
        )
        if (environment == ServiceEnvironment.TESTING) {
            env += "CONFP_IGNORE_ERRORS=true"
        }

        return linkedMapOf(
            "name" to "hermes-sidecar",
            "alivePolicy" to "Always",
            "availabilityPolicy" to "Required",
            "alloc" to linkedMapOf(
                "vcores" to "0.5",
                "mem" to "1G",
            ),
            "env" to env,
            "image" to linkedMapOf(
                "name" to "hermes-sidecar",
                "version" to version,
                "login" to "public",
            ),
            "ports" to linkedMapOf(
                "182" to "lan,tcp,started",
            ),
            "timeouts" to linkedMapOf(
                "deploy" to "5m",
                "start" to "5m",
                "stop" to "2m",
            ),
            "mounts" to linkedMapOf(
                "ephemeral" to "/ephemeral",
            ),
        )
    }

    private fun buildVectorSidecar(
        recommenderName: String,
        applicationName: String,
        pmsAppName: String,
        version: String = "129",
    ): Map<String, Any> {
        return linkedMapOf(
            "name" to "vector",
            "alivePolicy" to "Always",
            "availabilityPolicy" to "None",
            "alloc" to linkedMapOf(
                "vcores" to "0.4",
                "mem" to "1G",
            ),
            "env" to listOf(
                "PING_URL=http://localhost:8189/health",
                "PMS_APP_NAME=$pmsAppName",
                "RECOMMENDS_VECTOR_ENABLED=true",
                "RECOM_NAME=$recommenderName",
                "ZEN_APPLICATION=$applicationName",
                "ZEN_ENVIRONMENT=production",
            ),
            "image" to linkedMapOf(
                "name" to "public_vector_sidecar",
                "version" to version,
                "login" to "public",
            ),
            "mounts" to linkedMapOf(
                "ephemeral" to "/ephemeral",
            ),
        )
    }
}
