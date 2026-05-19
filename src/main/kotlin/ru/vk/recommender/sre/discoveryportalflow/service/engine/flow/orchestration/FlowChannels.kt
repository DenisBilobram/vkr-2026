package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration

import kotlinx.coroutines.channels.Channel
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity

@Component
class FlowChannels {

    val taskStatusChangedChannel = Channel<TaskStatusChangedEvent>(Channel.UNLIMITED)
    val stageStatusChangedChannel = Channel<StageStatusChangedEvent>(Channel.UNLIMITED)
    val pipelineStatusChangedChannel = Channel<PipelineStatusChangedEvent>(Channel.UNLIMITED)
    val taskRunChannel = Channel<TaskRunEntity>(Channel.UNLIMITED)

}
