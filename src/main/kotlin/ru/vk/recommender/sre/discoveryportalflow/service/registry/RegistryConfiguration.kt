package ru.vk.recommender.sre.discoveryportalflow.service.registry

import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.RegistryProjectPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.service.RegistryVerticalPersistenceService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.registry.plugin.FillProjectRegistryTask
import ru.vk.recommender.sre.discoveryportalflow.service.registry.plugin.FillVerticalRegistryTask

@Configuration(proxyBeanMethods = false)
class RegistryConfiguration {

    @FlowTaskBean(name = ["fillProjectRegistryTask"])
    fun fillProjectRegistryTask(
        registryProjectPersistenceService: RegistryProjectPersistenceService,
    ): FillProjectRegistryTask {
        return FillProjectRegistryTask(registryProjectPersistenceService)
    }

    @FlowTaskBean(name = ["fillVerticalRegistryTask"])
    fun fillVerticalRegistryTask(
        registryVerticalPersistenceService: RegistryVerticalPersistenceService,
    ): FillVerticalRegistryTask {
        return FillVerticalRegistryTask(registryVerticalPersistenceService)
    }
}
