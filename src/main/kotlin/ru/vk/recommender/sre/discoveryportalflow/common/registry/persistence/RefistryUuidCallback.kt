package ru.vk.recommender.sre.discoveryportalflow.common.registry.persistence

import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RegistryUuidCallback : BeforeConvertCallback<RegistryUidEntity> {

    override fun onBeforeConvert(aggregate: RegistryUidEntity): RegistryUidEntity {
        if (aggregate.uid == null) {
            aggregate.uid = UUID.randomUUID()
        }
        return aggregate
    }
}
