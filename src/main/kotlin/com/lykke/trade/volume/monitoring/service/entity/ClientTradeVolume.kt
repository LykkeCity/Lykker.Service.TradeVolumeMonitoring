package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal
import java.util.*

data class ClientTradeVolume (val clientId: String,
                              val assetId: String,
                              val volume: BigDecimal,
                              val timestamp: Date)