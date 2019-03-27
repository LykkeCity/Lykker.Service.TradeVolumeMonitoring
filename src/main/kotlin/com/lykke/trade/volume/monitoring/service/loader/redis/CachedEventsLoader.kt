package com.lykke.trade.volume.monitoring.service.loader.redis

import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CachedEventsLoader(private val eventsLoader: EventsLoader) : EventsLoader {

    private var events: List<EventPersistenceData>? = null
    private val lock = ReentrantLock()

    override fun loadEvents(): List<EventPersistenceData> {
        lock.withLock {
            var events = this.events
            if (events == null) {
                events = eventsLoader.loadEvents()
                this.events = events
            }
            return events
        }
    }

    fun clear() {
        lock.withLock {
            events = null
        }
    }
}