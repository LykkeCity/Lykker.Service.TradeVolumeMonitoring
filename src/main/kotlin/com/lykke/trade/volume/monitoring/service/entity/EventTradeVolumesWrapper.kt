package com.lykke.trade.volume.monitoring.service.entity

class EventTradeVolumesWrapper(val eventSequenceNumber: Long,
                               val tradeVolumes: List<TradeVolume>)