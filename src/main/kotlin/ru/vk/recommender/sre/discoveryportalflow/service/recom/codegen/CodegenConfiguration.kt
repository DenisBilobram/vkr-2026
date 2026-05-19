package ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskBean
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.generator.ProtoGenerator
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.plugin.ServiceCodegenTask
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.service.GrpcProxyCodegenSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.service.ServiceCodegenSupport
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.validator.ServiceTemplateCoverageValidator
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer.ServiceInfoWriter
import ru.vk.recommender.sre.discoveryportalflow.service.recom.codegen.writer.ServiceTemplateTreeRenderer
import ru.vk.recommender.sre.discoveryportalflow.service.recom.definition.RecomServiceRegistry
import ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver.ServiceRuntimeDefinitionResolver

@Configuration(proxyBeanMethods = false)
class CodegenConfiguration {

    @Bean
    fun protoGenerator(
        serviceTemplateTreeRenderer: ServiceTemplateTreeRenderer,
    ): ProtoGenerator {
        return ProtoGenerator(serviceTemplateTreeRenderer)
    }

    @Bean
    fun serviceTemplateTreeRenderer(): ServiceTemplateTreeRenderer {
        return ServiceTemplateTreeRenderer()
    }

    @Bean
    fun serviceInfoWriter(
        serviceTemplateTreeRenderer: ServiceTemplateTreeRenderer,
    ): ServiceInfoWriter {
        return ServiceInfoWriter(serviceTemplateTreeRenderer)
    }

    @Bean
    fun grpcProxyCodegenSupport(): GrpcProxyCodegenSupport {
        return GrpcProxyCodegenSupport()
    }

    @Bean
    fun serviceCodegenSupport(
        protoGenerator: ProtoGenerator,
        serviceTemplateTreeRenderer: ServiceTemplateTreeRenderer,
        serviceInfoWriter: ServiceInfoWriter,
    ): ServiceCodegenSupport {
        return ServiceCodegenSupport(
            protoGenerator = protoGenerator,
            templateTreeRenderer = serviceTemplateTreeRenderer,
            serviceInfoWriter = serviceInfoWriter,
        )
    }

    @Bean
    fun serviceTemplateCoverageValidator(
        recomServiceRegistry: RecomServiceRegistry,
    ): ServiceTemplateCoverageValidator {
        return ServiceTemplateCoverageValidator(recomServiceRegistry)
    }

    @FlowTaskBean
    fun serviceCodegenTask(
        serviceTemplateCoverageValidator: ServiceTemplateCoverageValidator,
        serviceRuntimeDefinitionResolver: ServiceRuntimeDefinitionResolver,
        protoGenerator: ProtoGenerator,
        gitlabClient: GitlabClient
    ): ServiceCodegenTask {
        return ServiceCodegenTask(
            gitlabClient,
            protoGenerator,
            serviceTemplateCoverageValidator,
            serviceRuntimeDefinitionResolver
        )
    }
}
