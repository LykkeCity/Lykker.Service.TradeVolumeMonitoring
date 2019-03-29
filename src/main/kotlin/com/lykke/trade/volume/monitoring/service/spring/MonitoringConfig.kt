package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponseGetter
import com.lykke.trade.volume.monitoring.service.isalive.impl.IsAliveResponseGetterImpl
import com.lykke.trade.volume.monitoring.service.monitoring.MonitoringStatsCollector
import com.lykke.utils.AppVersion
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MonitoringConfig {

    @Bean
    fun monitoringStatsCollector(): MonitoringStatsCollector {
        return MonitoringStatsCollector()
    }

    @Bean
    fun isAliveResponseGetter(monitoringStatsCollector: MonitoringStatsCollector): IsAliveResponseGetter {
        return IsAliveResponseGetterImpl(AppVersion.VERSION, monitoringStatsCollector)
    }
}