package com.lykke.trade.volume.monitoring.service.entity

class PersistenceData(val eventId: String,
                      val tradeVolumes: List<TradeVolumePersistenceData>)