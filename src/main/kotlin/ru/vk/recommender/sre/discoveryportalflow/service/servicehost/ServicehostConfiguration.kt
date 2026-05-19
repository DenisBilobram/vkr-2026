package ru.vk.recommender.sre.discoveryportalflow.service.servicehost

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.client.ServicehostAdminClient
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.client.ServicehostNodesParser
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.config.ServicehostAdminProperties
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.plugin.CreateServicehostClusterMR
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.plugin.CreateServicehostOfflineVerticalMR
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.plugin.CreateServicehostVerticalMR
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.mr.ServicehostClusterMrService
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.service.mr.ServicehostVerticalMrService
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostApiPayloadWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostBackendWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostGraphWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostRoutingWriter
import ru.vk.recommender.sre.discoveryportalflow.service.servicehost.writer.ServicehostShootingTestsWriter

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ServicehostAdminProperties::class)
class ServicehostConfiguration {

    @FlowTaskBean(name = ["createServicehostClusterMR"])
    fun createServicehostClusterMR(
        servicehostClusterMrService: ServicehostClusterMrService,
    ): CreateServicehostClusterMR {
        return CreateServicehostClusterMR(servicehostClusterMrService)
    }

    @FlowTaskBean(name = ["createServicehostVerticalMR"])
    fun createServicehostVerticalMR(
        servicehostVerticalMrService: ServicehostVerticalMrService,
    ): CreateServicehostVerticalMR {
        return CreateServicehostVerticalMR(servicehostVerticalMrService)
    }

    @FlowTaskBean(name = ["createServicehostOfflineVerticalMR"])
    fun createServicehostOfflineVerticalMR(
        servicehostVerticalMrService: ServicehostVerticalMrService,
    ): CreateServicehostOfflineVerticalMR {
        return CreateServicehostOfflineVerticalMR(servicehostVerticalMrService)
    }

    @Bean
    fun servicehostAdminClient(
        objectMapper: ObjectMapper,
        servicehostAdminProperties: ServicehostAdminProperties,
    ): ServicehostAdminClient {
        return ServicehostAdminClient(objectMapper, servicehostAdminProperties)
    }

    @Bean
    fun servicehostNodesParser(
        servicehostAdminClient: ServicehostAdminClient
    ): ServicehostNodesParser {
        return ServicehostNodesParser(servicehostAdminClient)
    }

    @Bean
    fun servicehostApiPayloadWriter(
        objectMapper: ObjectMapper,
    ): ServicehostApiPayloadWriter {
        return ServicehostApiPayloadWriter(objectMapper)
    }

    @Bean
    fun servicehostBackendWriter(): ServicehostBackendWriter {
        return ServicehostBackendWriter()
    }

    @Bean
    fun servicehostGraphWriter(): ServicehostGraphWriter {
        return ServicehostGraphWriter()
    }

    @Bean
    fun servicehostRoutingWriter(): ServicehostRoutingWriter {
        return ServicehostRoutingWriter()
    }

    @Bean
    fun servicehostShootingTestsWriter(gitlabClient: GitlabClient): ServicehostShootingTestsWriter {
        return ServicehostShootingTestsWriter(gitlabClient)
    }

    @Bean
    fun servicehostClusterMrService(
        servicehostApiPayloadWriter: ServicehostApiPayloadWriter,
        servicehostAdminClient: ServicehostAdminClient,
    ): ServicehostClusterMrService {
        return ServicehostClusterMrService(servicehostApiPayloadWriter, servicehostAdminClient)
    }

    @Bean
    fun servicehostVerticalMrService(
        servicehostBackendWriter: ServicehostBackendWriter,
        servicehostRoutingWriter: ServicehostRoutingWriter,
        servicehostGraphWriter: ServicehostGraphWriter,
        servicehostApiPayloadWriter: ServicehostApiPayloadWriter,
        servicehostShootingTestsWriter: ServicehostShootingTestsWriter,
        servicehostAdminClient: ServicehostAdminClient,
    ): ServicehostVerticalMrService {
        return ServicehostVerticalMrService(
            servicehostBackendWriter = servicehostBackendWriter,
            servicehostRoutingWriter = servicehostRoutingWriter,
            servicehostGraphWriter = servicehostGraphWriter,
            servicehostApiPayloadWriter = servicehostApiPayloadWriter,
            servicehostShootingTestsWriter = servicehostShootingTestsWriter,
            servicehostAdminClient = servicehostAdminClient,
        )
    }

}
