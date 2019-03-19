package com.lykke.trade.volume.monitoring.service.spring

import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponseGetter
import com.lykke.trade.volume.monitoring.service.isalive.impl.IsAliveResponseGetterImpl
import com.lykke.utils.AppVersion
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class IsAliveConfig {

    @Bean
    fun isAliveResponseGetter(): IsAliveResponseGetter {
        return IsAliveResponseGetterImpl(AppVersion.VERSION)
    }
}