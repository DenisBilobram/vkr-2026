package ru.vk.recommender.sre.discoveryportalflow.service.recom.resolver

import java.nio.file.Path

object WorkspaceResolver {
    val workspaceRoot: Path = Path.of("/app/workspace")

    fun resolve(workspaceRoot: Path, relativePath: String): Path {
        return workspaceRoot.resolve(relativePath)
    }

    fun resolveOrchestratorPath(orchestratorRelativePath: String): Path {
        val orchestratorModuleRootInRepository = workspaceRoot.resolve(MASTER_REPOSITORY_ORCHESTRATOR_PATH)
        return orchestratorModuleRootInRepository.resolve(orchestratorRelativePath)
    }

    private val MASTER_REPOSITORY_ORCHESTRATOR_PATH = Path.of("recommender", "public", "sre", "discovery-portal-flow")
}
