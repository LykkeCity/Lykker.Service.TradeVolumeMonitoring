package com.lykke.trade.volume.monitoring.service

import com.lykke.trade.volume.monitoring.service.config.AzureNotificationConfig
import com.lykke.trade.volume.monitoring.service.config.ClientAccountsConfig
import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.config.HttpApiConfig
import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.config.RabbitConfig
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeCacheConfig
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeConfig
import com.lykke.trade.volume.monitoring.service.entity.AssetDictionarySource
import com.lykke.trade.volume.monitoring.service.entity.MailApiType
import com.lykke.trade.volume.monitoring.service.utils.equalsIgnoreScale
import java.math.BigDecimal

fun assertEquals(expected: BigDecimal?, actual: BigDecimal?, message: String? = null) {
    if (expected == null && actual == null)  {
        return
    }

    if (!equalsIgnoreScale(expected!!, actual!!)) {
        kotlin.test.assertEquals(expected, actual, message)
    }
}

fun getConfig(volumePeriod: Long = 100, expiryRatio: Int = 2, cleanVolumeCacheInterval: Long = 100,
              notificationPeriod: Long = 100, maxVolume: BigDecimal = BigDecimal.valueOf(1000)): Config {
    return Config(TradeVolumeConfig(TradeVolumeCacheConfig(volumePeriod, expiryRatio, cleanVolumeCacheInterval),
            ClientAccountsConfig(RabbitConfig("test", "test", "test", "test"), HttpApiConfig("test", 1000)),
            NotificationsConfig(
                    MailApiType.Azure,
                    AzureNotificationConfig("testConnectionString", "mailQueue"),
                    HttpApiConfig("testUrl", 100),
                    notificationPeriod,
                    "test@sender.com",
                    listOf("test1@mail.com", "test2@mail.com")),
            maxVolume,
            "USD",
            emptySet(),
            AssetDictionarySource.PublicApi,
            null,
            "",
            0))
}