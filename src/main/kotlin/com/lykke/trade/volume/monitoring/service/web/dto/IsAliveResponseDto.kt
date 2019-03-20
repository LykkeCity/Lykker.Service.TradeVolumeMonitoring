package com.lykke.trade.volume.monitoring.service.web.dto

import io.swagger.annotations.ApiModel

@ApiModel("IsAliveResponse")
class IsAliveResponseDto(@JvmField val Version: String,
                         @JvmField val MonitoringStats: MonitoringStatsDto?)

