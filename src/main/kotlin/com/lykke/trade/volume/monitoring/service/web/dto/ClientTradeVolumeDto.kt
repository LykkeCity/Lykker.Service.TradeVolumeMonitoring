package com.lykke.trade.volume.monitoring.service.web.dto

import java.math.BigDecimal
import java.util.*

class ClientTradeVolumeDto(val clientId: String,
                           val assetId: String,
                           val volume: BigDecimal,
                           val timestamp: Date)