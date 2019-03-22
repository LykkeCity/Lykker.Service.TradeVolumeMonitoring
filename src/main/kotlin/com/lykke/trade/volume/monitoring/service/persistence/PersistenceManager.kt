package com.lykke.trade.volume.monitoring.service.persistence

import com.lykke.trade.volume.monitoring.service.entity.PersistenceData

interface PersistenceManager {
    fun persist(data: PersistenceData)
}