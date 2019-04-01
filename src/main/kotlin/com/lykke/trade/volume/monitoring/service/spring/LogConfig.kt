package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.logging.LogInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LogConfig {

    @Bean(initMethod = "init")
    fun logInitializer(config: Config): LogInitializer {
        return LogInitializer(config.slackNotifications, config.throttlingLogger)
    }

}