package com.lykke.trade.volume.monitoring.service.isalive.impl

import com.lykke.trade.volume.monitoring.service.entity.IsAliveResponse
import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponseGetter
import com.lykke.trade.volume.monitoring.service.monitoring.MonitoringStatsCollector

class IsAliveResponseGetterImpl(private val version: String,
                                private val monitoringStatsCollector: MonitoringStatsCollector) : IsAliveResponseGetter {
    override fun getResponse(): IsAliveResponse {
        return IsAliveResponse(true,
                version,
                monitoringStatsCollector.collect())
    }
}