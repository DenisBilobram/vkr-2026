package ru.vk.recommender.sre.discoveryportalflow.service.recom

import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ProjectRecommenderRuntimeContextFactory
import ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin.BootstrapRuntimeContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.plugin.ProjectRecommenderRuntimeBootstrapTask

@Configuration(proxyBeanMethods = false)
class RecomConfiguration {

    @FlowTaskBean
    fun bootstrapRuntimeContext(
        recomRuntimeContextFactory: RecomRuntimeContextFactory,
    ): BootstrapRuntimeContextTask {
        return BootstrapRuntimeContextTask(recomRuntimeContextFactory)
    }

    @FlowTaskBean
    fun bootstrapProjectRecomRuntimeContext(
        projectRecommenderRuntimeContextFactory: ProjectRecommenderRuntimeContextFactory,
    ): ProjectRecommenderRuntimeBootstrapTask {
        return ProjectRecommenderRuntimeBootstrapTask(projectRecommenderRuntimeContextFactory)
    }
}
