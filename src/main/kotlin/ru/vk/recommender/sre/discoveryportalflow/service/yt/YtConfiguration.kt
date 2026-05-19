package ru.vk.recommender.sre.discoveryportalflow.service.yt

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.yt.client.YtRuntimeClientFactory
import ru.vk.recommender.sre.discoveryportalflow.service.yt.plugin.BootstrapYtMockTablesContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.yt.plugin.YtMockTablesTask
import ru.vk.recommender.sre.discoveryportalflow.service.yt.service.YtMockTablesService

@Configuration(proxyBeanMethods = false)
class YtConfiguration {

    @FlowTaskBean
    fun bootstrapYtMockTablesContextTask(): BootstrapYtMockTablesContextTask {
        return BootstrapYtMockTablesContextTask()
    }

    @FlowTaskBean
    fun ytMockTablesTask(
        ytMockTablesService: YtMockTablesService,
    ): YtMockTablesTask {
        return YtMockTablesTask(ytMockTablesService)
    }

    @Bean
    fun ytRuntimeClientFactory(): YtRuntimeClientFactory {
        return YtRuntimeClientFactory()
    }

    @Bean
    fun ytMockTablesService(
        ytRuntimeClientFactory: YtRuntimeClientFactory,
    ): YtMockTablesService {
        return YtMockTablesService(ytRuntimeClientFactory)
    }
}
