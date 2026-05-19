package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.entity.vertical

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence.RegistryUidEntity

@Table(schema = "registry", name = "vertical_general_info")
data class RegistryVerticalGeneralInfoEntity(
    @Id
    @Column("uid")
    override var uid: UUID? = null,

    @Column("vertical_uid")
    val verticalUid: UUID,

    @Column("display_name")
    val displayName: String? = null,

    @Column("service_owner")
    val serviceOwner: String?,

    @Column("product_id")
    val productId: Int?,

    @Column("prm")
    val prm: String?,

    @Column("hermes_project_name")
    val hermesProjectName: String?,

    @Column("dictionary_base_project")
    val dictionaryBaseProject: String?,

    @Column("additional_responsibles")
    val additionalResponsibles: Array<String>,

    @Column("additional_followers")
    val additionalFollowers: Array<String>,

    @Column("servicehost_cluster_name")
    val servicehostClusterName: String?,

    @Column("teams_chat_id")
    val teamsChatId: String?,

    @Column("yt_cluster")
    val ytCluster: String?,

    @Column("toggles_online_tenant")
    val togglesOnlineTenant: String?,

    @Column("toggles_offline_tenant")
    val togglesOfflineTenant: String?,

    @Column("links")
    val links: String,
) : RegistryUidEntity
