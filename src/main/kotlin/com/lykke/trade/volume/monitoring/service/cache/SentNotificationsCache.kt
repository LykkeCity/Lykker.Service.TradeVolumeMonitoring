package com.lykke.trade.volume.monitoring.service.cache

import com.lykke.trade.volume.monitoring.service.entity.SentNotificationRecord

interface SentNotificationsCache {
    fun add(clientId: String, assetId: String)
    fun isSent(clientId: String, assetId: String): Boolean
    fun getAllNotificationsForLastTradePeriod(): List<SentNotificationRecord>
}