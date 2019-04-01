package com.lykke.trade.volume.monitoring.service.web.dto

import java.util.Date

class TradeVolumeLimitNotificationDto(val clientId: String,
                                      val assetId: String,
                                      val timestamp: Date)