package ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.platform.impl

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.recom.ServiceType
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.service.platform.PlatformService
import ru.vk.recommender.sre.discoveryportalflow.service.recom.model.api.impl.PlatformGatewayServiceConfig
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.RecommenderRuntime
import ru.vk.recommender.sre.discoveryportalflow.service.recom.runtime.ServiceRuntime

/**
 * Документация по изменению сервиса:
 * @see ru.vk.recommender.sre.discoveryportalflow.service.recom.CodeGenDoc
 */
@Component
class PlatformGatewayServiceDefinition : PlatformService<PlatformGatewayServiceConfig>(
    type = ServiceType.PLATFORM_GATEWAY,
    serviceName = "gateway",
    configClass = PlatformGatewayServiceConfig::class,
) {

    override fun resolveTemplateReplacements(
        recommenderRuntime: RecommenderRuntime,
        serviceRuntime: ServiceRuntime,
        serviceConfig: PlatformGatewayServiceConfig,
    ): Map<String, String> {
        return buildMap {
            putAll(super.resolveTemplateReplacements(recommenderRuntime, serviceRuntime, serviceConfig))
            put("\${ShardsAmount}", serviceConfig.baseShardsAmount.toString())
        }
    }
}
