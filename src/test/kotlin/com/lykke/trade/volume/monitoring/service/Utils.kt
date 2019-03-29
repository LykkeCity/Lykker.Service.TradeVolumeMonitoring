package com.lykke.trade.volume.monitoring.service

import com.lykke.trade.volume.monitoring.service.config.AzureNotificationConfig
import com.lykke.trade.volume.monitoring.service.config.HttpApiConfig
import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeCacheConfig
import com.lykke.trade.volume.monitoring.service.entity.MailApiType
import com.lykke.trade.volume.monitoring.service.utils.equalsIgnoreScale
import java.math.BigDecimal

fun assertEquals(expected: BigDecimal?, actual: BigDecimal?, message: String? = null) {
    if (expected == null && actual == null) {
        return
    }

    if (!equalsIgnoreScale(expected!!, actual!!)) {
        kotlin.test.assertEquals(expected, actual, message)
    }
}

fun buildNotificationConfig(notificationPeriod: Long = 100): NotificationsConfig {
    return NotificationsConfig(
            MailApiType.Azure,
            AzureNotificationConfig("testConnectionString", "mailQueue"),
            HttpApiConfig("testUrl", 100),
            notificationPeriod,
            "test@sender.com",
            listOf("test1@mail.com", "test2@mail.com"))
}

fun buildTradeVolumeCacheConfig(volumePeriod: Long = 100,
                                expiryRatio: Int = 2,
                                cleanVolumeCacheInterval: Long = 100): TradeVolumeCacheConfig {
    return TradeVolumeCacheConfig(volumePeriod, expiryRatio, cleanVolumeCacheInterval)
}