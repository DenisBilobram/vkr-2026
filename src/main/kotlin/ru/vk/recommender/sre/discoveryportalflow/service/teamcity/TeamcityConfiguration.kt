package ru.vk.recommender.sre.discoveryportalflow.service.teamcity

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.vk.recommender.sre.discoveryportalflow.service.hermes.config.HermesProperties
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.client.TeamCityClient
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.config.TeamcityProperties
import ru.vk.recommender.sre.discoveryportalflow.service.teamcity.orchestration.TaskCreator

@Configuration
@EnableConfigurationProperties(TeamcityProperties::class)
class TeamcityConfiguration {
    @Bean
    fun teamCityClient(teamcityProperties: TeamcityProperties): TeamCityClient {
        return TeamCityClient(teamcityProperties)
    }

    @Bean
    fun taskCreator(teamCityClient: TeamCityClient): TaskCreator {
        return TaskCreator(teamCityClient)
    }
}
