package ru.vk.recommender.sre.discoveryportalflow.service.pms.service.submit

import ru.vk.recommender.sre.discoveryportalflow.service.pms.context.ServicePmsTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceEnvironment
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.template.TemplateFileRenderer
import ru.vk.recommender.sre.discoveryportalflow.service.recom.util.parseNames

object GrpcProxyPmsConfigBuilder {

    fun buildConfiguration(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        environment: ServiceEnvironment,
    ): String {
        return TemplateFileRenderer.render(
            "templates/genericrecom/pms/grpc_proxy_configuration.yaml",
            buildTemplateValues(taskContext, serviceRuntime, environment, useRecommenderServiceName = false),
        ).trimEnd()
    }

    fun buildConfigurationPatch(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        environment: ServiceEnvironment,
    ): String {
        return TemplateFileRenderer.render(
            "templates/genericrecom/pms/grpc_proxy_configuration_patch.yaml",
            buildTemplateValues(taskContext, serviceRuntime, environment, useRecommenderServiceName = true),
        ).trimEnd()
    }

    fun buildBackendWeights(
        taskContext: ServicePmsTaskContext,
        environment: ServiceEnvironment,
    ): String {
        val clusterName = taskContext.clusterName

        return when (environment) {
            ServiceEnvironment.PRODUCTION -> listOf(
                "rc.public-$clusterName-servicehost.mesh.local:33",
                "dc.public-$clusterName-servicehost.mesh.local:33",
                "kc.public-$clusterName-servicehost.mesh.local:33",
                "public-$clusterName-canary-servicehost.mesh.local:1",
                "/dev/null:-1",
            )

            ServiceEnvironment.TESTING -> listOf(
                "public-$clusterName-stage-servicehost.mesh.local:100",
                "/dev/null:-1",
            )

            ServiceEnvironment.CANARY -> error("grpc-proxy does not support canary PMS configuration")
        }.joinToString(separator = "\n")
    }

    private fun buildMirroringBlock(
        projectName: String,
        environment: ServiceEnvironment,
    ): String {
        if (environment != ServiceEnvironment.PRODUCTION) {
            return ""
        }

        return """
mirroring:
  enabled: false
  host: $projectName.nda.example.invalid
  port: 80
  deadline-ms: 1000
  executor:
    max-threads: 8
    queue-size: 1000

""".trimIndent() + "\n"
    }

    private fun buildConnectionAgingBlock(environment: ServiceEnvironment): String {
        if (environment != ServiceEnvironment.PRODUCTION) {
            return ""
        }

        return """
  connection-aging:
    enabled: true
    max-connection-age-seconds: 300
    max-connection-age-grace-seconds: 30
""".trimIndent() + "\n"
    }

    private fun buildServicehostEndpoints(
        clusterName: String,
        environment: ServiceEnvironment,
    ): String {
        return if (environment == ServiceEnvironment.TESTING) {
            "public-$clusterName-stage-servicehost.mesh.local"
        } else {
            listOf(
                "dc.public-$clusterName-servicehost.mesh.local",
                "kc.public-$clusterName-servicehost.mesh.local",
                "rc.public-$clusterName-servicehost.mesh.local",
                "public-$clusterName-canary-servicehost.mesh.local",
            ).joinToString(separator = ",")
        }
    }

    private fun buildTemplateValues(
        taskContext: ServicePmsTaskContext,
        serviceRuntime: ServiceRuntime,
        environment: ServiceEnvironment,
        useRecommenderServiceName: Boolean,
    ): Map<String, String> {
        val projectName = requireNotNull(taskContext.projectName) {
            "recommender.projectName is required to configure grpc-proxy PMS for ${serviceRuntime.cloudServiceName}"
        }
        val projectNames = serviceRuntime.names
        val recommenderNames = parseNames(taskContext.recommenderName)
        val publicServiceOwnerClassName = if (useRecommenderServiceName) {
            recommenderNames.className
        } else {
            projectNames.className
        }

        return mapOf(
            "APP_NAME" to serviceRuntime.cloudServiceName,
            "PROFILE_NAME" to projectNames.packageName,
            "MIRRORING_BLOCK" to buildMirroringBlock(projectName, environment),
            "CONNECTION_AGING_BLOCK" to buildConnectionAgingBlock(environment),
            "SERVICEHOST_ENDPOINT_NAME" to "public-servicehost-${taskContext.recommenderName}",
            "SERVICEHOST_WEIGHT_FILE_NAME" to "grpc-http-adapter-backend-weights-servicehost-${taskContext.recommenderName}",
            "SERVICEHOST_ENDPOINTS" to buildServicehostEndpoints(
                clusterName = taskContext.clusterName,
                environment = environment,
            ),
            "PUBLIC_SERVICE_NAME" to "public.recommender.${publicServiceOwnerClassName}Recommender",
            "PUBLIC_METHOD_NAME" to "Recommend${recommenderNames.className}",
            "PUMPKIN_METHOD_NAME" to "Recommend${recommenderNames.className}Pumpkin",
            "HANDLE_ALIAS" to "recommend-${taskContext.recommenderName}",
            "PUMPKIN_HANDLE_ALIAS" to "recommend-${taskContext.recommenderName}-pumpkin",
            "PUBLIC_PATH" to "/api/v1/export-${taskContext.recommenderName}",
            "PUMPKIN_PATH" to "/api-v1/recommend-pumpkin-${taskContext.recommenderName}",
        )
    }
}
