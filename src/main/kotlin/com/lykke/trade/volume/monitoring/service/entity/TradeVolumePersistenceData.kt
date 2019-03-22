package com.lykke.trade.volume.monitoring.service.entity

import java.math.BigDecimal
import java.util.Date

class TradeVolumePersistenceData(val eventSequenceNumber: Long,
                                 val tradeIdx: Int,
                                 val clientId: String,
                                 val assetId: String,
                                 val targetAssetVolume: BigDecimal,
                                 val timestamp: Date)