package com.lykke.trade.volume.monitoring.service.entity

import java.io.Serializable
import java.util.Date

class EventPersistenceData(val sequenceNumber: Long,
                           val timestamp: Date,
                           val tradeVolumes: List<TradeVolumePersistenceData>) : Serializable