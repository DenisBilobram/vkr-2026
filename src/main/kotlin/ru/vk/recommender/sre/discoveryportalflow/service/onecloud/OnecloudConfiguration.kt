package ru.vk.recommender.sre.discoveryportalflow.service.onecloud

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.builder.OnecloudDeploymentContextBuilder
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.client.OnecloudClient
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.config.OnecloudProperties
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin.BootstrapOnecloudDeploymentContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin.OnecloudSubmitQueuesTask
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin.OnecloudSubmitServicesTask
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin.OnecloudSubmitStoragesTask
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin.OnecloudWaitServicesCreatedTask
import ru.vk.recommender.sre.discoveryportalflow.service.onecloud.plugin.OnecloudWaitServicesRunningTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OnecloudProperties::class)
class OnecloudConfiguration {

    @FlowTaskBean
    fun bootstrapOnecloudDeploymentContext(
        onecloudDeploymentContextBuilder: OnecloudDeploymentContextBuilder,
        recomRuntimeContextFactory: RecomRuntimeContextFactory,
    ): BootstrapOnecloudDeploymentContextTask {
        return BootstrapOnecloudDeploymentContextTask(
            onecloudDeploymentContextBuilder = onecloudDeploymentContextBuilder,
            recomRuntimeContextFactory = recomRuntimeContextFactory,
        )
    }

    @Bean
    fun onecloudDeploymentContextBuilder(
        objectMapper: ObjectMapper,
        serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
    ): OnecloudDeploymentContextBuilder {
        return OnecloudDeploymentContextBuilder(
            objectMapper = objectMapper,
            serviceRuntimeDefinitionResolver = serviceRuntimeDefinitionResolver,
        )
    }

    @FlowTaskBean
    fun onecloudSubmitQueuesTask(onecloudClient: OnecloudClient): OnecloudSubmitQueuesTask {
        return OnecloudSubmitQueuesTask(onecloudClient)
    }

    @FlowTaskBean
    fun onecloudSubmitServicesTask(onecloudClient: OnecloudClient): OnecloudSubmitServicesTask {
        return OnecloudSubmitServicesTask(onecloudClient)
    }

    @FlowTaskBean
    fun onecloudSubmitStoragesTask(onecloudClient: OnecloudClient): OnecloudSubmitStoragesTask {
        return OnecloudSubmitStoragesTask(onecloudClient)
    }

    @FlowTaskBean
    fun onecloudWaitServicesCreatedTask(onecloudClient: OnecloudClient): OnecloudWaitServicesCreatedTask {
        return OnecloudWaitServicesCreatedTask(onecloudClient)
    }

    @FlowTaskBean
    fun onecloudWaitServicesRunningTask(onecloudClient: OnecloudClient): OnecloudWaitServicesRunningTask {
        return OnecloudWaitServicesRunningTask(onecloudClient)
    }

    @Bean
    fun onecloudClient(
        objectMapper: ObjectMapper,
        onecloudProperties: OnecloudProperties,
    ): OnecloudClient {
        return OnecloudClient(objectMapper, onecloudProperties)
    }
}
