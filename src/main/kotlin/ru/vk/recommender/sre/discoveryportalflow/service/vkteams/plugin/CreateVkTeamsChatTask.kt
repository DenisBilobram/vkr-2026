package ru.vk.recommender.sre.discoveryportalflow.service.vkteams.plugin

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker.TaskRunResult
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.VkteamsExtendedClient
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.client.request.CreateChatRequest
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.context.CreateVkTeamsChatContext
import ru.vk.recommender.sre.discoveryportalflow.service.vkteams.plugin.config.VkTeamsTaskProperties

@Component("createVkTeamsChatTask")
class CreateVkTeamsChatTask(
    private val vkTeamsClient: VkteamsExtendedClient,
    private val props: VkTeamsTaskProperties,
) : FlowTask<CreateVkTeamsChatContext>(CreateVkTeamsChatContext::class) {

    override suspend fun executeCasted(taskRunContext: CreateVkTeamsChatContext): TaskRunResult {
        val recommenderName = taskRunContext.recommender.recommenderName
        runtimeLogger.info("Creating VK Teams chat for recommender: $recommenderName")
        if (!props.isChatCreateAllowed) { // kill switch
            runtimeLogger.info("Creating VK Teams chats is disabled by config")
            return TaskRunResult(taskStatus = FlowStatus.SUCCEEDED)
        }
        val usersToAdd = collectUsers(taskRunContext)
        val members = usersToAdd.map { user ->
            mapOf("sn" to user)
        }
        val chatAbout = "Chat for recommender: $recommenderName"
        val createChatRequest = CreateChatRequest(
            name = recommenderName,
            about = chatAbout,
            isPublic = props.areChatsPublic,
            members = members
        )

        runtimeLogger.info("Creating chat with name: $recommenderName")
        val chatResponse = vkTeamsClient.createChat(createChatRequest)

        if (!chatResponse.ok || chatResponse.sn == null) {
            runtimeLogger.error("Failed to create chat: ${chatResponse.description}")
            return TaskRunResult(taskStatus = FlowStatus.FAILED)
        }

        val chatId = chatResponse.sn
        runtimeLogger.info("Chat created successfully with ID: $chatId")

        taskRunContext.teamsChatId = chatId
        return TaskRunResult(
            taskStatus = FlowStatus.SUCCEEDED,
        )
    }


    // NOTE: members must use the format expected by the configured messenger implementation.
    private suspend fun collectUsers(context: CreateVkTeamsChatContext): List<String> {
        val users = mutableSetOf<String>()

        users.addAll(context.teamsUsersToAdd)
        val finalUsers = users.take(props.maxUsersAddAllowed)

        if (users.size > props.maxUsersAddAllowed) {
            runtimeLogger.warn("Limiting users from ${users.size} to $finalUsers as per configuration")
        }

        runtimeLogger.info("Adding final request user: $finalUsers")
        runtimeLogger.info("Adding mandatory users: ${props.mandatoryUsersToAdd}")
        return finalUsers + props.mandatoryUsersToAdd
    }

}
