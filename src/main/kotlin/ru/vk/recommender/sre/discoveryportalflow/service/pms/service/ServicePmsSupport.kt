package ru.vk.recommender.sre.discoveryportalflow.service.pms.service

import ru.vk.recommender.sre.discoveryportalflow.service.pms.client.PmsClient
import ru.vk.recommender.sre.discoveryportalflow.service.pms.config.PmsProperties
import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit.YtProxyPmsConfigBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceScope
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.util.OneSecretPathBuilder

class ServicePmsSupport(
    private val pmsClient: PmsClient,
    private val pmsProperties: PmsProperties,
) {
    fun submitApptracerProperties(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
    ) {
        val onecloudDirectoryName = serviceRuntime.onecloudDirectoryName
        val pmsApplicationName = serviceRuntime.pmsApplicationName
        val apptracerSecretId = resolveApptracerSecretId(taskContext, serviceRuntime)

        val commonProperties = mapOf(
            "tracer.vector.http.uri" to pmsProperties.apptracerUploadUri,
            "tracer.vector.source" to "/ephemeral/var/log/$onecloudDirectoryName/tracer.log",
        )

        pmsClient.updateProperties(
            applicationName = pmsApplicationName,
            onecloudPmsHosts = listOf(serviceRuntime.environment(ServiceEnvironment.PRODUCTION).pmsHost),
            properties = commonProperties + (
                "tracer.vector.crashToken" to
                    "${OneSecretPathBuilder.build(serviceRuntime.environment(ServiceEnvironment.PRODUCTION), apptracerSecretId)}:appToken"
                ),
        )

        if (serviceRuntime.supports(ServiceEnvironment.CANARY)) {
            pmsClient.updateProperties(
                applicationName = pmsApplicationName,
                onecloudPmsHosts = listOf(serviceRuntime.environment(ServiceEnvironment.CANARY).pmsHost),
                properties = commonProperties + (
                    "tracer.vector.crashToken" to
                        "${OneSecretPathBuilder.build(serviceRuntime.environment(ServiceEnvironment.CANARY), apptracerSecretId)}:appToken"
                    ),
            )
        }
    }

    fun submitYtProxyConfig(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
    ) {
        pmsClient.updateProperties(
            applicationName = serviceRuntime.pmsApplicationName,
            onecloudPmsHosts = resolveYtProxyHosts(serviceRuntime),
            properties = mapOf(
                "app-config" to YtProxyPmsConfigBuilder.build(taskContext),
            ),
        )
    }

    fun submitPropertiesByEnvironment(
        serviceRuntime: ServiceRuntime,
        propertiesByEnvironment: Map<ServiceEnvironment, Map<String, String>>,
    ) {
        val pmsApplicationName = serviceRuntime.pmsApplicationName

        propertiesByEnvironment.forEach { (environment, properties) ->
            if (environment != ServiceEnvironment.PRODUCTION && !serviceRuntime.supports(environment)) {
                return@forEach
            }
            if (properties.isEmpty()) {
                return@forEach
            }

            pmsClient.updateProperties(
                applicationName = pmsApplicationName,
                onecloudPmsHosts = listOf(serviceRuntime.environment(environment).pmsHost),
                properties = properties,
            )
        }
    }

    private fun resolveYtProxyHosts(serviceRuntime: ServiceRuntime): List<String> {
        return buildList {
            add(serviceRuntime.environment(ServiceEnvironment.PRODUCTION).pmsHost)
            if (serviceRuntime.supports(ServiceEnvironment.CANARY)) {
                add(serviceRuntime.environment(ServiceEnvironment.CANARY).pmsHost)
            }
            if (serviceRuntime.supports(ServiceEnvironment.TESTING)) {
                add(serviceRuntime.environment(ServiceEnvironment.TESTING).pmsHost)
            }
        }
    }

    private fun resolveApptracerSecretId(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
    ): String {
        return if (serviceRuntime.scope == ServiceScope.PROJECT_SCOPED) {
            requireNotNull(taskContext.projectOneSecretId) {
                "projectOneSecretId is required to configure PMS for project-scoped service " +
                    serviceRuntime.cloudServiceName
            }
        } else {
            requireNotNull(taskContext.recomOneSecretId) {
                "recomOneSecretId is required to configure PMS for vertical-scoped service " +
                    serviceRuntime.cloudServiceName
            }
        }
    }
}
