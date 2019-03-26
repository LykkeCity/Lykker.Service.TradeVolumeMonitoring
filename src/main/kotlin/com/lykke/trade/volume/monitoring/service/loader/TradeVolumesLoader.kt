package com.lykke.trade.volume.monitoring.service.loader

import com.lykke.trade.volume.monitoring.service.entity.PersistenceData

// todo implement
interface TradeVolumesLoader {
    fun loadPersistenceData(): List<PersistenceData>
}