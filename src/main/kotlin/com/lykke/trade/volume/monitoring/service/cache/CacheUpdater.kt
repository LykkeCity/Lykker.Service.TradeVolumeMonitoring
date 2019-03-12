package com.lykke.trade.volume.monitoring.service.cache

import kotlin.concurrent.fixedRateTimer

class CacheUpdater(private val dataCaches: Collection<DataCache>) {
    fun start() {
        dataCaches.forEach { dataCache ->
            fixedRateTimer(name = "${dataCache::class.java.name}.Updater",
                    initialDelay = dataCache.updateInterval,
                    period = dataCache.updateInterval) {
                dataCache.update()
            }
        }
    }
}