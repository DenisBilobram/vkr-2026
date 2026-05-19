package ru.vk.recommender.sre.discoveryportalflow.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.vk.recommender.sre.discoveryportalflow.persistence.model.FlowContextEntity
import java.util.UUID

interface FlowContextRepository : CrudRepository<FlowContextEntity, UUID>
