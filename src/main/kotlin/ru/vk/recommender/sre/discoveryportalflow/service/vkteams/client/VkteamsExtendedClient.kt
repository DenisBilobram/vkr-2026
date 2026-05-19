package ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.config.VkTeamsClientProperties
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.request.CreateChatRequest
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.response.CreateChatResponse

@Component
class VkteamsExtendedClient(
    private val props: VkTeamsClientProperties,
    private val objectMapper: ObjectMapper,
) {

    fun createChat(request: CreateChatRequest): CreateChatResponse {
        // NDA code removed: production implementation calls an internal messenger bot API.
        objectMapper.createObjectNode()
            .put("baseUrl", props.baseUrl)
            .put("name", request.name)
        return CreateChatResponse(
            sn = request.name?.let { "nda-chat-$it" },
            ok = true,
            description = "NDA code removed",
        )
    }
}
