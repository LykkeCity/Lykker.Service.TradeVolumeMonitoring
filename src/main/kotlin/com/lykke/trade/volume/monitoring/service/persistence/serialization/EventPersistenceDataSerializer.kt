package com.lykke.trade.volume.monitoring.service.persistence.serialization

import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData

interface EventPersistenceDataSerializer {
    fun serialize(eventPersistenceData: EventPersistenceData): ByteArray
    fun deserialize(byteArray: ByteArray): EventPersistenceData
}