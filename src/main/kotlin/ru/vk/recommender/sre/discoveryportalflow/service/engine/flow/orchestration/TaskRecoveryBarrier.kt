package ru.vk.recommender.sre.discoveryportalflow.service.engine.flow.orchestration

import kotlinx.coroutines.CompletableDeferred
import org.springframework.stereotype.Component

@Component
class TaskRecoveryBarrier {
    private val recoveryCompleted = CompletableDeferred<Unit>()

    suspend fun awaitRecoveryCompleted() {
        recoveryCompleted.await()
    }

    fun markRecoveryCompleted() {
        recoveryCompleted.complete(Unit)
    }
}
