package ru.vk.recommender.sre.discoveryportalflow.service.toggles.service

import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.client.GitlabClient
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabCommitDraft
import ru.vk.recommender.sre.discoveryportalflow.service.gitlab.model.GitlabMergeRequestDraft
import ru.vk.recommender.sre.discoveryportalflow.service.toggles.config.TogglesProperties

data class TogglesTenantRolesDraft(
    val commitDraft: GitlabCommitDraft,
    val mergeRequestDraft: GitlabMergeRequestDraft,
)

class TogglesTenantRolesService(
    private val togglesProperties: TogglesProperties,
    private val gitlabClient: GitlabClient,
) {

    fun shouldUpdateRoles(tenantName: String): Boolean {
        val file = gitlabClient.getFile(gitlabClient.getRepositoryProjectId(), getRolesFilePath())
        return !file.content!!.contains("\"${tenantName}-toggles\"") ||
                !file.content!!.contains("\"${tenantName}-vk-drills\"")
    }

    fun buildRolesDraft(tenantName: String, owner: String): TogglesTenantRolesDraft {
        val repositoryProjectId = gitlabClient.getRepositoryProjectId()
        val rolesFilePath = getRolesFilePath()
        val jiraTask = requireNotNull(togglesProperties.jiraTaskRoleCreation) {
            "toggles.jira-task-role-creation must be configured"
        }
        val file = gitlabClient.updateFile(repositoryProjectId, rolesFilePath) { content ->
            var updated = insertRoleIntoValuesSection(content, createTogglesRoleString(tenantName, owner))
            updated = insertRoleIntoValuesSection(updated, createDrillsRoleString(tenantName, owner))
            return@updateFile updated
        }

        val branch = "${jiraTask}_${tenantName}_toggles"
        val message = "$jiraTask: $tenantName tenant creation"

        return TogglesTenantRolesDraft(
            commitDraft = GitlabCommitDraft(
            projectId = repositoryProjectId,
                branch = branch,
                commitMessage = message,
                files = listOf(file),
            ),
            mergeRequestDraft = GitlabMergeRequestDraft(
            projectId = repositoryProjectId,
                sourceBranch = branch,
                title = message,
            ),
        )
    }

    private fun getRolesFilePath(): String {
        return requireNotNull(togglesProperties.zeusRolesJsonPath) {
            "toggles.zeus-roles-json-path must be configured"
        }
    }

    private fun insertRoleIntoValuesSection(content: String, roleStr: String): String {
        val lines = content.split("\n")
        val result = mutableListOf<String>()

        var foundValuesLine = false
        var insideAllRolesSection = false

        for (line in lines) {
            result.add(line)
            if ("\"allRoles\"" in line) {
                insideAllRolesSection = true
            }
            if (insideAllRolesSection && !foundValuesLine && line.contains("\"values\"") && line.contains(":") && line.contains("{")) {
                result.add(roleStr)
                foundValuesLine = true
            }
        }

        return result.joinToString("\n")
    }

    private fun createTogglesRoleString(tenantName: String, owner: String): String {
        return """            "$tenantName-toggles": {
                "name": "РўРµРЅР°РЅС‚ С‚РѕРіР»РѕРІ $tenantName",
                "owners": [
                    "d.pogorelov",
                    "mikh.nikiforov",
                    "denis.a.ivanov",
                    "svc-disc-port-gitlab",
                    "$owner"
                ],
                "slug": "$tenantName-toggles",
                "roles": {
                    "name": "Р РѕР»СЊ",
                    "slug": "role",
                    "values": {
                        "admin": {
                            "name": "РђРґРјРёРЅРёСЃС‚СЂР°С‚РѕСЂ",
                            "slug": "admin",
                            "help": "РњРѕР¶РµС‚ СѓРїСЂР°РІР»СЏС‚СЊ РЅР°СЃС‚СЂРѕР№РєР°РјРё РєРѕРЅС„РёРіР°"
                        },
                        "user": {
                            "name": "РџРѕР»СЊР·РѕРІР°С‚РµР»СЊ",
                            "slug": "user",
                            "help": "РњРѕР¶РµС‚ РёСЃРїРѕР»СЊР·РѕРІР°С‚СЊ Р°РґРјРёРЅРєСѓ"
                        },
                        "release_manager": {
                            "name": "Р РµР»РёР· РјРµРЅРµРґР¶РµСЂ",
                            "slug": "release_manager",
                            "help": "РћС‚РІРµС‚СЃС‚РІРµРЅРЅС‹Р№ Р·Р° СЂРёР»РёР·С‹"
                        },
                        "ee_expert": {
                            "name": "Р­РєСЃРїРµСЂС‚ EE",
                            "slug": "ee_expert",
                            "help": "РћС‚РІРµС‚СЃС‚РІРµРЅРЅС‹Р№ Р·Р° СЌРєСЃРїРµСЂРёРјРµРЅС‚С‹"
                        }
                    }
                }
            },"""
    }

    private fun createDrillsRoleString(tenantName: String, owner: String): String {
        return """            "$tenantName-vk-drills": {
            "name": "РђРґРјРёРЅРєР° $tenantName Drills",
                "owners": [
                    "d.pogorelov",
                    "mikh.nikiforov",
                    "denis.a.ivanov",
                    "svc-disc-port-gitlab",
                    "$owner"
                ],
                "slug": "$tenantName-vk-drills",
                "roles": {
                    "name": "Р РѕР»СЊ",
                    "slug": "role",
                    "values": {
                        "admin": {
                            "name": "РђРґРјРёРЅРёСЃС‚СЂР°С‚РѕСЂ",
                            "slug": "admin",
                            "help": "РњРѕР¶РµС‚ СѓРїСЂР°РІР»СЏС‚СЊ РґСЂРёР»Р»Р·Р°РјРё РїСЂРѕРµРєС‚Р° $tenantName"
                        }
                    }
                }
            },"""
    }
}
