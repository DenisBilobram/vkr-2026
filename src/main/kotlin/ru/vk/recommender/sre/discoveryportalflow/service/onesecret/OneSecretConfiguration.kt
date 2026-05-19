package ru.vk.recommender.sre.discoveryportalflow.service.onesecret

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.client.OneSecretClient
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.client.OneSecretProperties
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.AppTracerInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.RedisInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.codec.YtInfoOneSecretCodec
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.BootstrapOneSecretContextTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.create.CreateProjectSecretTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.create.CreateServiceSecretTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.create.CreateVerticalSecretTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read.ReadAppTracerTokenFromSecretTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read.ReadRedisInfoFromSecretTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.read.ReadYtInfoFromSecretTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.plugin.update.UpdateSecretValuesTask
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretProvisioningService
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretQueueTargetResolver
import ru.vk.recommender.sre.discoveryportalflow.service.onesecret.service.OneSecretService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecomRuntimeContextFactory

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OneSecretProperties::class)
class OneSecretConfiguration {

    @FlowTaskBean(name = ["bootstrapOneSecretContext"])
    fun bootstrapOneSecretContext(
        oneSecretQueueTargetResolver: OneSecretQueueTargetResolver,
        recomRuntimeContextFactory: RecomRuntimeContextFactory,
    ): BootstrapOneSecretContextTask {
        return BootstrapOneSecretContextTask(oneSecretQueueTargetResolver, recomRuntimeContextFactory)
    }

    @FlowTaskBean
    fun createProjectSecretTask(
        oneSecretService: OneSecretService,
    ): CreateProjectSecretTask {
        return CreateProjectSecretTask(oneSecretService)
    }

    @FlowTaskBean
    fun createVerticalSecretTask(
        oneSecretService: OneSecretService,
    ): CreateVerticalSecretTask {
        return CreateVerticalSecretTask(oneSecretService)
    }

    @FlowTaskBean
    fun createServiceSecretTask(
        oneSecretProvisioningService: OneSecretProvisioningService,
    ): CreateServiceSecretTask {
        return CreateServiceSecretTask(oneSecretProvisioningService)
    }

    @FlowTaskBean
    fun updateSecretValuesTask(
        oneSecretService: OneSecretService,
    ): UpdateSecretValuesTask {
        return UpdateSecretValuesTask(oneSecretService)
    }

    @FlowTaskBean
    fun readYtInfoFromSecretTask(
        oneSecretService: OneSecretService,
        ytInfoOneSecretCodec: YtInfoOneSecretCodec,
    ): ReadYtInfoFromSecretTask {
        return ReadYtInfoFromSecretTask(oneSecretService, ytInfoOneSecretCodec)
    }

    @FlowTaskBean
    fun readRedisInfoFromSecretTask(
        oneSecretService: OneSecretService,
        redisInfoOneSecretCodec: RedisInfoOneSecretCodec,
    ): ReadRedisInfoFromSecretTask {
        return ReadRedisInfoFromSecretTask(oneSecretService, redisInfoOneSecretCodec)
    }

    @FlowTaskBean
    fun readAppTracerTokenFromSecretTask(
        oneSecretService: OneSecretService,
        appTracerInfoOneSecretCodec: AppTracerInfoOneSecretCodec,
    ): ReadAppTracerTokenFromSecretTask {
        return ReadAppTracerTokenFromSecretTask(oneSecretService, appTracerInfoOneSecretCodec)
    }

    @Bean
    fun oneSecretClient(
        objectMapper: ObjectMapper,
        oneSecretProperties: OneSecretProperties,
    ): OneSecretClient {
        return OneSecretClient(objectMapper, oneSecretProperties)
    }

    @Bean
    fun oneSecretService(
        oneSecretClient: OneSecretClient,
        ytInfoOneSecretCodec: YtInfoOneSecretCodec,
        appTracerInfoOneSecretCodec: AppTracerInfoOneSecretCodec,
    ): OneSecretService {
        return OneSecretService(oneSecretClient, ytInfoOneSecretCodec, appTracerInfoOneSecretCodec)
    }

    @Bean
    fun ytInfoOneSecretCodec(): YtInfoOneSecretCodec {
        return YtInfoOneSecretCodec()
    }

    @Bean
    fun redisInfoOneSecretCodec(): RedisInfoOneSecretCodec {
        return RedisInfoOneSecretCodec()
    }

    @Bean
    fun appTracerInfoOneSecretCodec(): AppTracerInfoOneSecretCodec {
        return AppTracerInfoOneSecretCodec()
    }

    @Bean
    fun oneSecretProvisioningService(
        serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
        oneSecretService: OneSecretService,
    ): OneSecretProvisioningService {
        return OneSecretProvisioningService(serviceRuntimeDefinitionResolver, oneSecretService)
    }

    @Bean
    fun oneSecretQueueTargetResolver(
        serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
    ): OneSecretQueueTargetResolver {
        return OneSecretQueueTargetResolver(serviceRuntimeDefinitionResolver)
    }
}
