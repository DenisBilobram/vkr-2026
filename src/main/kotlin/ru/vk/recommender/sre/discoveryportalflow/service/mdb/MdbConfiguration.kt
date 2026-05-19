package ru.vk.recommender.sre.discoveryportalflow.service.mdb

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.client.MdbClient
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.config.MdbProperties
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin.BootstrapSnapshotsBuilderMongoDbContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin.CreateMongoDatabaseTask
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin.CreateMongoUserTask
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.plugin.WaitMdbOperationTask
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoCredentialsFactory
import ru.vk.recommender.sre.discoveryportalflow.service.mdb.service.MdbMongoDbService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MdbProperties::class)
class MdbConfiguration {

    @FlowTaskBean(name = ["bootstrapSnapshotsBuilderMongoDbContextTask"])
    fun bootstrapSnapshotsBuilderMongoDbContextTask(
        mdbMongoCredentialsFactory: MdbMongoCredentialsFactory,
        recomRuntimeContextFactory: RecomRuntimeContextFactory,
    ): BootstrapSnapshotsBuilderMongoDbContextTask {
        return BootstrapSnapshotsBuilderMongoDbContextTask(mdbMongoCredentialsFactory, recomRuntimeContextFactory)
    }

    @FlowTaskBean
    fun createMongoDatabaseTask(
        mdbMongoDbService: MdbMongoDbService,
    ): CreateMongoDatabaseTask {
        return CreateMongoDatabaseTask(mdbMongoDbService)
    }

    @FlowTaskBean
    fun createMongoUserTask(
        mdbMongoDbService: MdbMongoDbService,
    ): CreateMongoUserTask {
        return CreateMongoUserTask(mdbMongoDbService)
    }

    @FlowTaskBean
    fun waitMdbOperationTask(
        mdbMongoDbService: MdbMongoDbService,
    ): WaitMdbOperationTask {
        return WaitMdbOperationTask(mdbMongoDbService)
    }

    @Bean
    fun mdbClient(
        objectMapper: ObjectMapper,
        mdbProperties: MdbProperties,
    ): MdbClient {
        return MdbClient(objectMapper, mdbProperties)
    }

    @Bean
    fun mdbMongoDbService(
        mdbClient: MdbClient,
        mdbProperties: MdbProperties,
    ): MdbMongoDbService {
        return MdbMongoDbService(mdbClient, mdbProperties)
    }

    @Bean
    fun mdbMongoCredentialsFactory(): MdbMongoCredentialsFactory {
        return MdbMongoCredentialsFactory()
    }
}
