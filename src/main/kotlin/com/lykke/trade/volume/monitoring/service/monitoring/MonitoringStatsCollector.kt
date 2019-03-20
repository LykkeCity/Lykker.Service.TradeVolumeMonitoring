package com.lykke.trade.volume.monitoring.service.monitoring

import com.lykke.trade.volume.monitoring.service.entity.MonitoringStats
import com.lykke.utils.logging.ThrottlingLogger
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

class MonitoringStatsCollector {

    companion object {
        private val LOGGER = ThrottlingLogger.getLogger(MonitoringStatsCollector::class.java.name)
        private const val MB = 1024 * 1024
    }

    fun collect(): MonitoringStats? {
        return try {
            val osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)
            val vmCpuLoad = osBean.processCpuLoad
            val totalCpuLoad = osBean.systemCpuLoad
            val totalMemory = osBean.totalPhysicalMemorySize / MB
            val freeMemory = osBean.freePhysicalMemorySize / MB
            val maxHeap = Runtime.getRuntime().maxMemory() / MB
            val freeHeap = Runtime.getRuntime().freeMemory() / MB
            val totalHeap = Runtime.getRuntime().totalMemory() / MB
            val totalSwap = osBean.totalSwapSpaceSize / MB
            val freeSwap = osBean.freeSwapSpaceSize / MB

            val threadsCount = Thread.getAllStackTraces().keys.size

            MonitoringStats(vmCpuLoad = vmCpuLoad,
                    totalCpuLoad = totalCpuLoad,
                    totalMemory = totalMemory,
                    freeMemory = freeMemory,
                    maxHeap = maxHeap,
                    freeHeap = freeHeap,
                    totalHeap = totalHeap,
                    totalSwap = totalSwap,
                    freeSwap = freeSwap,
                    threadsCount = threadsCount)
        } catch (e: Exception) {
            LOGGER.error("Unable to gather monitoring stats: ${e.message}", e)
            null
        }
    }
}