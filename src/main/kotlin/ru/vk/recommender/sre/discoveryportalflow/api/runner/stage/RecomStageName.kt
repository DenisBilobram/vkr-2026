package ru.vk.recommender.sre.discoveryportalflow.api.runner.stage

enum class RecomStageName(
    val pipelineName: String,
    val stageName: String,
) {
    PMS_SETTING("ServicesInfrastructureSetup", "pmsSetting"),
    ONESECRET_SETTING("ServicesInfrastructureSetup", "oneSecretSetting"),
    MDB_MONGO_SETTINGS("RecomInfrastructureSetup", "mdbMongoSettings"),
    OTHER_SETTING("RecomInfrastructureSetup", "otherSetting"),
    SERVICES_GENERATION("ServicesSetup", "servicesGeneration"),
    SERVICES_ONECLOUD_DEPLOYMENT("ServicesSetup", "servicesOnecloudDeployment"),
    TOGGLES_ONLINE_TENANT_CREATE("TogglesOnlineTenantSetup", "TogglesOnlineTenantCreate"),
    TOGGLES_ONLINE_TENANT_UPDATE("TogglesOnlineTenantSetup", "TogglesOnlineTenantUpdate"),
    TOGGLES_OFFLINE_TENANT_CREATE("TogglesOfflineTenantSetup", "TogglesOfflineTenantCreate"),
    TOGGLES_OFFLINE_TENANT_UPDATE("TogglesOfflineTenantSetup", "TogglesOfflineTenantUpdate"),
    HERMES_SNAPHOTS_SETTINGS("SnapshotsIntegration", "hermesSnaphotsSettings"),
    HERMER_SNAPSHOTS_SYNC("SnapshotsIntegration", "hermerSnapshotsSync"),
    PREPARE_RECOM_INFRA("ServicesInfrastructureSetup", "prepareRecomInfra"),
    RUNTIME_VERTICAL_GENERATION("VerticalRuntimeGeneration", ""),
    DICTIONARY_PROJECT_SETTING("RecomInfrastructureSetup", "dictionaryProjectSetting"),
    SERVICEHOST_SETTING("RecomInfrastructureSetup", "servicehostSetting"),
}
