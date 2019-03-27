package com.lykke.trade.volume.monitoring.service.loader

import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData

interface EventsLoader {
    fun loadEvents(): List<EventPersistenceData>
}