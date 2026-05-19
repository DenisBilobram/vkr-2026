package ru.vk.recommender.sre.discoveryportalflow.service.pms

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.pms.client.PmsClient
import ru.vk.recommender.sre.discoveryportalflow.service.pms.config.PmsProperties
import ru.vk.recommender.sre.discoveryportalflow.service.pms.plugin.ServicePmsSubmitTask
import ru.vk.recommender.sre.discoveryportalflow.service.pms.service.ServicePmsSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PmsProperties::class)
class PmsConfiguration {

    @FlowTaskBean
    fun servicePmsSubmitTask(
        serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
    ): ServicePmsSubmitTask {
        return ServicePmsSubmitTask(serviceRuntimeDefinitionResolver)
    }

    @Bean
    fun pmsClient(
        objectMapper: ObjectMapper,
        pmsProperties: PmsProperties,
    ): PmsClient {
        return PmsClient(objectMapper, pmsProperties)
    }

    @Bean
    fun servicePmsSupport(
        pmsClient: PmsClient,
        pmsProperties: PmsProperties,
    ): ServicePmsSupport {
        return ServicePmsSupport(pmsClient, pmsProperties)
    }
}
