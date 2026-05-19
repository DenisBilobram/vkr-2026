package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties

data class StageDefinitionProperties(
    val stageName: String,
    val executeIf: String? = null,
    val taskDefinitions: List<TaskDefinitionProperties> = emptyList(),
) {

    fun enabledTaskDefinitions(): List<TaskDefinitionProperties> {
        return taskDefinitions.filterNot { definition -> definition.disabled }
    }
}
