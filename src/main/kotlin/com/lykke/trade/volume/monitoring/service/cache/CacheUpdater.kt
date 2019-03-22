package com.lykke.trade.volume.monitoring.service.cache

import org.springframework.scheduling.TaskScheduler
import java.time.Duration
import java.time.ZonedDateTime

class CacheUpdater(private val dataCaches: Collection<DataCache>,
                   private val taskScheduler: TaskScheduler) {
    fun start() {
        dataCaches.forEach { dataCache ->
            taskScheduler.scheduleAtFixedRate(dataCache::update,
                    ZonedDateTime.now().toInstant().plusMillis(dataCache.updateInterval),
                    Duration.ofMillis(dataCache.updateInterval))
        }
    }
}