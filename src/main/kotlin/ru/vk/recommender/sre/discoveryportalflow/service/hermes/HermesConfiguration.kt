package ru.vk.recommender.sre.discoveryportalflow.service.hermes

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.client.SnapshotsBuilderClient
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.config.HermesProperties
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin.BootstrapHermesSnapshotCopyContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin.BuildProdHermesSnapshotsTask
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin.CopyHermesSnapshotMetaTask
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin.GenerateHermesGroupMappingsTask
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.plugin.WaitHermesProdSnapshotsTask
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotService
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.service.HermesSnapshotTypeResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory
import java.net.http.HttpClient

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(HermesProperties::class)
class HermesConfiguration {

    @FlowTaskBean(name = ["bootstrapHermesSnapshotCopyContextTask"])
    fun bootstrapHermesSnapshotCopyContextTask(
        recomRuntimeContextFactory: RecomRuntimeContextFactory,
        hermesSnapshotTypeResolver: HermesSnapshotTypeResolver,
    ): BootstrapHermesSnapshotCopyContextTask {
        return BootstrapHermesSnapshotCopyContextTask(recomRuntimeContextFactory, hermesSnapshotTypeResolver)
    }

    @FlowTaskBean(name = ["buildProdHermesSnapshotsTask"])
    fun buildProdHermesSnapshotsTask(
        hermesSnapshotService: HermesSnapshotService,
    ): BuildProdHermesSnapshotsTask {
        return BuildProdHermesSnapshotsTask(hermesSnapshotService)
    }

    @FlowTaskBean(name = ["waitHermesProdSnapshotsTask"])
    fun waitHermesProdSnapshotsTask(
        hermesSnapshotService: HermesSnapshotService,
    ): WaitHermesProdSnapshotsTask {
        return WaitHermesProdSnapshotsTask(hermesSnapshotService)
    }

    @FlowTaskBean(name = ["copyHermesSnapshotMetaTask"])
    fun copyHermesSnapshotMetaTask(
        hermesSnapshotService: HermesSnapshotService,
    ): CopyHermesSnapshotMetaTask {
        return CopyHermesSnapshotMetaTask(hermesSnapshotService)
    }

    @FlowTaskBean(name = ["generateHermesGroupMappingsTask"])
    fun generateHermesGroupMappingsTask(
        hermesSnapshotService: HermesSnapshotService,
    ): GenerateHermesGroupMappingsTask {
        return GenerateHermesGroupMappingsTask(hermesSnapshotService)
    }

    @Bean
    fun hermesSnapshotTypeResolver(): HermesSnapshotTypeResolver {
        return HermesSnapshotTypeResolver()
    }

    @Bean
    fun snapshotsBuilderClient(
        objectMapper: ObjectMapper,
        grafanaHttpClient: HttpClient,
    ): SnapshotsBuilderClient {
        return SnapshotsBuilderClient(objectMapper, grafanaHttpClient)
    }

    @Bean
    fun hermesSnapshotService(
        hermesProperties: HermesProperties,
        snapshotsBuilderClient: SnapshotsBuilderClient
    ): HermesSnapshotService {
        return HermesSnapshotService(hermesProperties, snapshotsBuilderClient)
    }
}
