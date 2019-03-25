package com.lykke.trade.volume.monitoring.service.notification

interface NotificationService {
    fun sendTradeVolumeLimitReachedMailNotification(clientId: String, assetId: String)
}