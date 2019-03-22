package com.lykke.trade.volume.monitoring.service.cache

interface DataCache {
    val updateInterval: Long
    fun update()
}