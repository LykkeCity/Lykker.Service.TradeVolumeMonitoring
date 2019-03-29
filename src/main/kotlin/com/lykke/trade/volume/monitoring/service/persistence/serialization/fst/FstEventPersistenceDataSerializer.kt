package com.lykke.trade.volume.monitoring.service.persistence.serialization.fst

import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.persistence.serialization.EventPersistenceDataSerializer
import org.nustaq.serialization.FSTConfiguration

class FstEventPersistenceDataSerializer : EventPersistenceDataSerializer {

    private val fstConfiguration: FSTConfiguration = FSTConfiguration.createDefaultConfiguration()

    override fun serialize(eventPersistenceData: EventPersistenceData): ByteArray {
        return fstConfiguration.asByteArray(eventPersistenceData)
    }

    override fun deserialize(byteArray: ByteArray): EventPersistenceData {
        return fstConfiguration.asObject(byteArray) as EventPersistenceData
    }
}