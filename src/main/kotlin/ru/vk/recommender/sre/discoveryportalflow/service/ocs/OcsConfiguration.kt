package ru.vk.recommender.sre.discoveryportalflow.service.ocs

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.client.OcsClient
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.plugin.BootstrapOcsContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.plugin.DictionaryProjectTask
import ru.vk.recommender.sre.discoveryportalflow.service.ocs.service.DictionaryProjectService

@Configuration(proxyBeanMethods = false)
class OcsConfiguration {

    @FlowTaskBean(name = ["bootstrapOcsContext"])
    fun bootstrapOcsContext(): BootstrapOcsContextTask {
        return BootstrapOcsContextTask()
    }

    @FlowTaskBean
    fun dictionaryProjectTask(
        dictionaryProjectService: DictionaryProjectService,
    ): DictionaryProjectTask {
        return DictionaryProjectTask(dictionaryProjectService)
    }

    @Bean
    fun ocsClient(
        objectMapper: ObjectMapper,
    ): OcsClient {
        return OcsClient(objectMapper)
    }

    @Bean
    fun dictionaryProjectService(
        ocsClient: OcsClient,
    ): DictionaryProjectService {
        return DictionaryProjectService(ocsClient)
    }
}
