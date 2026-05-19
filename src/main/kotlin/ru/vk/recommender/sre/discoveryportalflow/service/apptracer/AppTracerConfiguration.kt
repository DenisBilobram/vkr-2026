package ru.vk.recommender.sre.discoveryportalflow.service.apptracer

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.client.AppTracerClient
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.config.TracerClientProperties
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.plugin.CreateAppTracerProjectTask
import ru.vk.recommender.sre.discoveryportalflow.service.apptracer.service.AppTracerProjectService
import ru.vk.recommender.sre.discoveryportalflow.service.engine.logging.TasksRuntimeLogger
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TracerClientProperties::class)
class AppTracerConfiguration {

    @Bean
    fun appTracerClient(
        objectMapper: ObjectMapper,
        tracerClientProperties: TracerClientProperties,
    ): AppTracerClient {
        return AppTracerClient(objectMapper, tracerClientProperties)
    }

    @Bean
    fun appTracerProjectService(
        appTracerClient: AppTracerClient,
        objectMapper: ObjectMapper,
        tasksRuntimeLogger: TasksRuntimeLogger,
    ): AppTracerProjectService {
        return AppTracerProjectService(appTracerClient, objectMapper, tasksRuntimeLogger)
    }

    @FlowTaskBean(name = ["createAppTracerProjectTask"])
    fun createAppTracerProjectTask(
        appTracerProjectService: AppTracerProjectService,
    ): CreateAppTracerProjectTask {
        return CreateAppTracerProjectTask(appTracerProjectService)
    }
}
