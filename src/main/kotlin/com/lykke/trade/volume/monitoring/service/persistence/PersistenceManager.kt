package com.lykke.trade.volume.monitoring.service.persistence

import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData

interface PersistenceManager {
    fun persist(eventPersistenceData: EventPersistenceData)
}