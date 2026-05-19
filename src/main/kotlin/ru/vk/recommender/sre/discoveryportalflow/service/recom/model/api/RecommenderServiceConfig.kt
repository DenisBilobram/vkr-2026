package ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.BaseServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.FactorProxyServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.GrpcProxyServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MediatorServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MetaI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.MetaServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.PlatformGatewayServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SchedulerI2IServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SelectorsServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.SnapshotsBuilderServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.WorkerServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.YtProxyServiceConfig
import kotlin.reflect.KClass

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = RecommenderServiceConfig::class,
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BaseServiceConfig::class, name = "BASE"),
    JsonSubTypes.Type(value = BaseI2IServiceConfig::class, name = "BASE_I2I"),
    JsonSubTypes.Type(value = GrpcProxyServiceConfig::class, name = "GRPC_PROXY"),
    JsonSubTypes.Type(value = MediatorServiceConfig::class, name = "MEDIATOR"),
    JsonSubTypes.Type(value = YtProxyServiceConfig::class, name = "YT_PROXY"),
    JsonSubTypes.Type(value = FactorProxyServiceConfig::class, name = "FACTOR_PROXY"),
    JsonSubTypes.Type(value = WorkerServiceConfig::class, name = "WORKER"),
    JsonSubTypes.Type(value = PlatformGatewayServiceConfig::class, name = "PLATFORM_GATEWAY"),
    JsonSubTypes.Type(value = MetaServiceConfig::class, name = "META"),
    JsonSubTypes.Type(value = MetaI2IServiceConfig::class, name = "META_I2I"),
    JsonSubTypes.Type(value = SelectorsServiceConfig::class, name = "SELECTORS"),
    JsonSubTypes.Type(value = SnapshotsBuilderServiceConfig::class, name = "SNAPSHOTS_BUILDER"),
    JsonSubTypes.Type(value = SchedulerI2IServiceConfig::class, name = "SCHEDULER_I2I"),
)
open class RecommenderServiceConfig(
    open val type: ServiceType,
    open val serviceDisabled: Boolean = false,
    open val hasSnapshots: Boolean = false,
    open val hasVector: Boolean = true,
) {

    fun <T : Any> requireConfig(
        expectedType: KClass<T>,
    ): T {
        val expectedJavaClass = expectedType.java
        if (!expectedJavaClass.isInstance(this)) {
            error(
                "Expected config ${expectedType.simpleName}, got ${this::class.simpleName}",
            )
        }

        return expectedJavaClass.cast(this)
    }
}
