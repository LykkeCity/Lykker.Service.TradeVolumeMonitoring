package com.lykke.trade.volume.monitoring.service.web.dto

import io.swagger.annotations.ApiModel

@ApiModel("MonitoringStats")
class MonitoringStatsDto(@JvmField val VmCpuLoad: Double,
                         @JvmField val TotalCpuLoad: Double,
                         @JvmField val TotalMemory: Long,
                         @JvmField val FreeMemory: Long,
                         @JvmField val MaxHeap: Long,
                         @JvmField val TotalHeap: Long,
                         @JvmField val FreeHeap: Long,
                         @JvmField val TotalSwap: Long,
                         @JvmField val FreeSwap: Long,
                         @JvmField val ThreadsCount: Int)