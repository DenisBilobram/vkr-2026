package ru.vk.recommender.sre.discoveryportalflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    exclude = [
        MongoAutoConfiguration::class,
        MongoDataAutoConfiguration::class,
    ],
)
@EnableScheduling
class DiscoveryPortalFlowApplication

fun main(args: Array<String>) {
	runApplication<DiscoveryPortalFlowApplication>(*args)
}
