package com.lykke.trade.volume.monitoring.service.entity

import java.util.Date

class EventTradeVolumesWrapper(val eventSequenceNumber: Long,
                               val eventTimestamp: Date,
                               val tradeVolumes: List<TradeVolume>)