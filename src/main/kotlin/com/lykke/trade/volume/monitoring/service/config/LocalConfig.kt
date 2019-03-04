package com.lykke.trade.volume.monitoring.service.config

import com.lykke.utils.config.ConfigInitializer
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component("Config")
@Profile("local_config")
class LocalConfig : FactoryBean<TradeVolumeConfig> {
    override fun getObject(): TradeVolumeConfig {
        return ConfigInitializer.initConfig("local",  classOfT = TradeVolumeConfig::class.java)
    }

    override fun getObjectType(): Class<*> {
        return TradeVolumeConfig::class.java
    }
}