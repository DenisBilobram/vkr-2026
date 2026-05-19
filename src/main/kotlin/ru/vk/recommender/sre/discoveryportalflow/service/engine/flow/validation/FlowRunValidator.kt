package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.validation

import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.StageRunEntity
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.TaskRunEntity

@Component
class FlowRunValidator {

    fun requireStageCanStart(stageRun: StageRunEntity) {
        require(stageRun.status == FlowStatus.READY) {
            "Stage '${stageRun.requireStageName()}' must be READY to start, but was ${stageRun.status}"
        }
    }

    fun requireTaskCanRetry(taskRun: TaskRunEntity) {
        require(taskRun.status == FlowStatus.FAILED || taskRun.status == FlowStatus.PENDING) {
            "Only FAILED task runs can be retried manually, but task '${taskRun.taskName}' was ${taskRun.status}"
        }
    }

    fun requireTaskRunsNotCreated(
        stageRun: StageRunEntity,
        existingTaskRunsCount: Int,
    ) {
        require(existingTaskRunsCount == 0) {
            "Task graph is already materialized for stage run ${stageRun.requireId()}"
        }
    }

    fun requireStartedTaskRun(taskRun: TaskRunEntity): Long {
        return requireNotNull(taskRun.startedAt) {
            "Task run startedAt is required for execution context"
        }
    }
}
