package ru.vk.recommender.sre.discoveryportalflow.service.engine.task.worker

import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowStatus

data class TaskRunResult(val taskStatus: FlowStatus)
