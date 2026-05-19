package ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.validation

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.FlowProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineChildrenType
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.PipelineStageReferenceProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.StageDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.definition.properties.TaskDefinitionProperties
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTask
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowTaskContext
import ru.vk.recommender.sre.discoveryportalflow.service.engine.task.plugin.common.FlowWaitingTask

@Component
class FlowDefinitionValidator(
    private val flowProperties: FlowProperties,
    private val tasks: Map<String, FlowTask<out FlowTaskContext>>,
) {

    @PostConstruct
    fun validate() {
        validateStageDefinitions()
        validateBootstrapTaskDefinitions()

        val rootDefinitions = flowProperties.pipelineDefinitions
        val allDefinitions = flatten(rootDefinitions)

        rootDefinitions.forEach(::validatePipelineDefinition)
        validateUniquePipelineNames(allDefinitions)
        validatePipelineDependencyGraphs(rootDefinitions)
        validateStageDependencyGraphs(allDefinitions)
        validateTaskBeansAndPolicies()
    }

    private fun validatePipelineDefinition(definition: PipelineDefinitionProperties) {
        require(definition.pipelineName.isNotBlank()) { "Pipeline name must not be blank" }

        when (definition.childrenType) {
            PipelineChildrenType.PIPELINE -> {
                require(definition.stages.isEmpty()) {
                    "Structural pipeline '${definition.pipelineName}' must not declare stages"
                }
                require(definition.pipelines.isNotEmpty()) {
                    "Structural pipeline '${definition.pipelineName}' must declare child pipelines"
                }
                validateNoDuplicates(
                    items = definition.pipelines.map { childPipeline -> childPipeline.pipelineName },
                    subject = "Structural pipeline '${definition.pipelineName}' contains duplicate child pipelines",
                )
                validateSiblingPipelineDependencies(
                    siblingDefinitions = definition.pipelines,
                    subject = "Structural pipeline '${definition.pipelineName}'",
                )
                definition.pipelines.forEach(::validatePipelineDefinition)
            }

            PipelineChildrenType.STAGE -> {
                require(definition.pipelines.isEmpty()) {
                    "Executable pipeline '${definition.pipelineName}' must not declare child pipelines"
                }
                require(definition.stages.isNotEmpty()) {
                    "Executable pipeline '${definition.pipelineName}' must declare stages"
                }
                validateNoDuplicates(
                    items = definition.stages.map { stageDefinition -> stageDefinition.stageName },
                    subject = "Executable pipeline '${definition.pipelineName}' contains duplicate stage names",
                )
                definition.stages.forEach { stageReference ->
                    validateStageReference(definition.pipelineName, stageReference)
                }
            }
        }
    }

    private fun validateStageDefinitions() {
        val stageDefinitions = flowProperties.stageDefinitions
        validateNoDuplicates(
            items = stageDefinitions.map { definition -> definition.stageName },
            subject = "Flow contains duplicate stage definitions",
        )
        stageDefinitions.forEach(::validateStageDefinition)
    }

    private fun validateStageDefinition(stageDefinition: StageDefinitionProperties) {
        require(stageDefinition.stageName.isNotBlank()) {
            "Stage definition name must not be blank"
        }
        validateExecuteIfPath(
            executeIfPath = stageDefinition.executeIf,
            subject = "Stage definition '${stageDefinition.stageName}'",
        )

        validateNoDuplicates(
            items = stageDefinition.taskDefinitions.map { definition -> definition.taskName },
            subject = "Stage definition '${stageDefinition.stageName}' contains duplicate task names",
        )

        stageDefinition.taskDefinitions.forEach { taskDefinition ->
            validateTaskDefinition(stageDefinition.stageName, taskDefinition)
        }

        val enabledTaskDefinitions = stageDefinition.enabledTaskDefinitions()
        val enabledTaskDefinitionsByName = enabledTaskDefinitions.associateBy { definition -> definition.taskName }
        val invalidDependencies = enabledTaskDefinitions.flatMap { definition ->
            definition.dependencyTasks
                .distinct()
                .filterNot(enabledTaskDefinitionsByName::containsKey)
                .map { dependencyTaskName -> "${definition.taskName} -> $dependencyTaskName" }
        }
        require(invalidDependencies.isEmpty()) {
            "Stage definition '${stageDefinition.stageName}' contains enabled tasks with missing or disabled dependencies: ${invalidDependencies.joinToString()}"
        }
    }

    private fun validateBootstrapTaskDefinitions() {
        val bootstrapTaskDefinitions = flowProperties.bootstrapTaskDefinitions
        validateNoDuplicates(
            items = bootstrapTaskDefinitions.map { definition -> definition.taskName },
            subject = "Flow contains duplicate bootstrap task definitions",
        )
        bootstrapTaskDefinitions.forEach { definition ->
            validateTaskDefinition(stageName = "bootstrap task definitions", definition = definition)
            require(definition.dependencyTasks.isEmpty()) {
                "Bootstrap task '${definition.taskName}' must not declare dependency-tasks"
            }
            require(!definition.disabled) {
                "Bootstrap task '${definition.taskName}' must not be disabled"
            }
        }
    }

    private fun validateStageReference(
        pipelineName: String,
        stageReference: PipelineStageReferenceProperties,
    ) {
        require(stageReference.stageName.isNotBlank()) {
            "Stage name must not be blank in executable pipeline '$pipelineName'"
        }
        require(flowProperties.stageDefinitions.any { definition -> definition.stageName == stageReference.stageName }) {
            "Executable pipeline '$pipelineName' references unknown stage definition '${stageReference.stageName}'"
        }
        require(stageReference.dependencyStages.all { dependencyStage -> dependencyStage.isNotBlank() }) {
            "Stage '${stageReference.stageName}' in executable pipeline '$pipelineName' contains blank dependency names"
        }
        stageReference.bootstrapTask?.let { bootstrapTask ->
            require(bootstrapTask.isNotBlank()) {
                "Stage '${stageReference.stageName}' in executable pipeline '$pipelineName' contains blank bootstrap-task"
            }
            val bootstrapTaskDefinition = flowProperties.bootstrapTaskDefinitions.firstOrNull { definition ->
                definition.taskName == bootstrapTask
            } ?: error(
                "Stage '${stageReference.stageName}' in executable pipeline '$pipelineName' references unknown bootstrap task '$bootstrapTask'"
            )
            val stageDefinition = flowProperties.stageDefinitions.first { definition ->
                definition.stageName == stageReference.stageName
            }
            require(stageDefinition.taskDefinitions.none { definition -> definition.taskName == bootstrapTaskDefinition.taskName }) {
                "Stage '${stageReference.stageName}' in executable pipeline '$pipelineName' contains task name collision with bootstrap task '${bootstrapTaskDefinition.taskName}'"
            }
        }
    }

    private fun validateTaskDefinition(stageName: String, definition: TaskDefinitionProperties) {
        require(definition.taskName.isNotBlank()) {
            "Task name must not be blank in stage '$stageName'"
        }
        require(definition.taskBean.isNotBlank()) {
            "Task bean must not be blank for task '${definition.taskName}' in stage '$stageName'"
        }
        validateExecuteIfPath(
            executeIfPath = definition.executeIf,
            subject = "Task '${definition.taskName}' in stage '$stageName'",
        )
        require(definition.timeout.toMillis() > 0) {
            "Task '${definition.taskName}' in stage '$stageName' must have a positive timeout"
        }
        require(definition.retryPolicy.attempts > 0) {
            "Task '${definition.taskName}' in stage '$stageName' must have retry-policy.attempts > 0"
        }
        if (definition.waitingPolicy.waitingTask) {
            require(definition.waitingPolicy.delay.toMillis() > 0) {
                "Task '${definition.taskName}' in stage '$stageName' must have waiting-policy.delay > 0"
            }
        }
    }

    private fun validateUniquePipelineNames(definitions: List<PipelineDefinitionProperties>) {
        validateNoDuplicates(
            items = definitions.map { definition -> definition.pipelineName },
            subject = "Flow contains duplicate pipeline names",
        )
    }

    private fun validatePipelineDependencyGraphs(rootDefinitions: List<PipelineDefinitionProperties>) {
        validateSiblingPipelineDependencies(
            siblingDefinitions = rootDefinitions,
            subject = "Root flow",
        )
    }

    private fun validateSiblingPipelineDependencies(
        siblingDefinitions: List<PipelineDefinitionProperties>,
        subject: String,
    ) {
        if (siblingDefinitions.isEmpty()) {
            return
        }

        require(siblingDefinitions.all { definition ->
            definition.dependencyPipelines.all { dependencyPipeline -> dependencyPipeline.isNotBlank() }
        }) {
            "$subject contains blank pipeline dependency names"
        }

        val pipelinesByName = siblingDefinitions.associateBy { definition -> definition.pipelineName }
        val invalidDependencies = siblingDefinitions.flatMap { definition ->
            definition.dependencyPipelines
                .distinct()
                .filterNot(pipelinesByName::containsKey)
                .map { dependencyPipelineName -> "${definition.pipelineName} -> $dependencyPipelineName" }
        }
        require(invalidDependencies.isEmpty()) {
            "$subject contains unknown pipeline dependencies: ${invalidDependencies.joinToString()}"
        }

        val visiting = linkedSetOf<String>()
        val visited = linkedSetOf<String>()

        fun dfs(pipelineName: String) {
            if (pipelineName in visited) return
            require(visiting.add(pipelineName)) {
                "$subject contains a cycle in pipeline dependencies at '$pipelineName'"
            }
            pipelinesByName.getValue(pipelineName).dependencyPipelines.distinct().forEach(::dfs)
            visiting.remove(pipelineName)
            visited += pipelineName
        }

        pipelinesByName.keys.sorted().forEach(::dfs)
    }

    private fun validateStageDependencyGraphs(definitions: List<PipelineDefinitionProperties>) {
        definitions
            .filter { definition -> definition.childrenType == PipelineChildrenType.STAGE }
            .forEach { definition ->
                val stagesByName = definition.stages.associateBy { stageDefinition -> stageDefinition.stageName }
                val invalidDependencies = definition.stages.flatMap { stageDefinition ->
                    stageDefinition.dependencyStages
                        .distinct()
                        .filterNot(stagesByName::containsKey)
                        .map { dependencyStage -> "${stageDefinition.stageName} -> $dependencyStage" }
                }
                require(invalidDependencies.isEmpty()) {
                    "Executable pipeline '${definition.pipelineName}' contains unknown stage dependencies: ${invalidDependencies.joinToString()}"
                }

                val visiting = linkedSetOf<String>()
                val visited = linkedSetOf<String>()

                fun dfs(stageName: String) {
                    if (stageName in visited) return
                    require(visiting.add(stageName)) {
                        "Executable pipeline '${definition.pipelineName}' contains a cycle in stage dependencies at '$stageName'"
                    }
                    stagesByName.getValue(stageName).dependencyStages.distinct().forEach(::dfs)
                    visiting.remove(stageName)
                    visited += stageName
                }

                stagesByName.keys.sorted().forEach(::dfs)
            }
    }

    private fun validateTaskBeansAndPolicies() {
        flowProperties.stageDefinitions.forEach { stageDefinition ->
            stageDefinition.taskDefinitions.forEach { taskDefinition ->
                validateTaskBeanAndPolicy(
                    taskDefinition = taskDefinition,
                    subject = "task '${taskDefinition.taskName}' in stage definition '${stageDefinition.stageName}'",
                )
            }
        }
        flowProperties.bootstrapTaskDefinitions.forEach { taskDefinition ->
            validateTaskBeanAndPolicy(
                taskDefinition = taskDefinition,
                subject = "bootstrap task '${taskDefinition.taskName}'",
            )
        }
    }

    private fun validateTaskBeanAndPolicy(
        taskDefinition: TaskDefinitionProperties,
        subject: String,
    ) {
        val task = tasks[taskDefinition.taskBean] ?: error(
            "No FlowTask bean '${taskDefinition.taskBean}' found for $subject"
        )

        if (taskDefinition.waitingPolicy.waitingTask) {
            require(task is FlowWaitingTask<*>) {
                "$subject declares waiting-policy, but bean '${taskDefinition.taskBean}' is not a FlowWaitingTask"
            }
        } else {
            require(task !is FlowWaitingTask<*>) {
                "$subject uses bean '${taskDefinition.taskBean}', which is a FlowWaitingTask and requires waiting-policy.waiting-task: true"
            }
        }
    }

    private fun validateExecuteIfPath(executeIfPath: String?, subject: String) {
        executeIfPath ?: return
        require(executeIfPath.isNotBlank()) {
            "$subject must have a non-blank execute-if path"
        }
        require(executeIfPath.split('.').all { pathSegment -> pathSegment.isNotBlank() }) {
            "$subject has invalid execute-if path '$executeIfPath'"
        }
    }

    private fun validateNoDuplicates(items: List<String>, subject: String) {
        val duplicates = items
            .groupBy { item -> item }
            .filterValues { groupedItems -> groupedItems.size > 1 }
            .keys
            .sorted()
        require(duplicates.isEmpty()) {
            "$subject: ${duplicates.joinToString()}"
        }
    }

    private fun flatten(definitions: List<PipelineDefinitionProperties>): List<PipelineDefinitionProperties> {
        return definitions.flatMap { definition ->
            listOf(definition) + flatten(definition.pipelines)
        }
    }
}
