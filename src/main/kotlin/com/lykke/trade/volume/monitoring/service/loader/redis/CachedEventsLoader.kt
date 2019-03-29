package com.lykke.trade.volume.monitoring.service.loader.redis

import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader

class CachedEventsLoader(private val eventsLoader: EventsLoader) : EventsLoader {

    private var events: List<EventPersistenceData>? = null

    @Synchronized
    override fun loadEvents(): List<EventPersistenceData> {
        if (events == null) {
            events = eventsLoader.loadEvents()
        }
        return events ?: emptyList()
    }

    @Synchronized
    fun clear() {
        events = null
    }
}