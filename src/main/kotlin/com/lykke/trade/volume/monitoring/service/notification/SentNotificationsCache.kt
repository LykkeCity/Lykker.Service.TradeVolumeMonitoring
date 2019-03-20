package com.lykke.trade.volume.monitoring.service.notification

interface SentNotificationsCache {
    fun add(clientId: String, assetId: String)
    fun isSent(clientId: String, assetId: String)
}