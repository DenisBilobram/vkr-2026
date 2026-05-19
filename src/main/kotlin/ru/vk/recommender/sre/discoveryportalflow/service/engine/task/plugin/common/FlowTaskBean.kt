package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common

import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Bean
annotation class FlowTaskBean(
    @get:AliasFor(annotation = Bean::class, attribute = "name")
    val name: Array<String> = [],
)
