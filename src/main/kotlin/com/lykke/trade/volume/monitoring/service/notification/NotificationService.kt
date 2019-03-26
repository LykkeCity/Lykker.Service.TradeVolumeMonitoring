package com.lykke.trade.volume.monitoring.service.notification

import java.util.*

interface NotificationService {
    fun sendTradeVolumeLimitReachedMailNotification(clientId: String, assetId: String, timestamp: Date)
}