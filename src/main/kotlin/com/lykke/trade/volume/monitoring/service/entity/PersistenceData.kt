package com.lykke.trade.volume.monitoring.service.entity

class PersistenceData(val eventSequenceNumber: Long,
                      val tradeVolumes: List<TradeVolumePersistenceData>)