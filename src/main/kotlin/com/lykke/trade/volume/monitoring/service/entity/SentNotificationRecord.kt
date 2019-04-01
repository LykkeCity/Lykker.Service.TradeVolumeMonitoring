package com.lykke.trade.volume.monitoring.service.entity

import java.util.Date

data class SentNotificationRecord(val clientId: String, val assetId: String, val timestamp: Date)