package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.config.NotificationsConfig
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeCacheConfig
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeConfig
import org.junit.Before
import org.junit.Test

class SentNotificationsCacheTest {

    private companion object {
        val CLIENT1 = "CLIENT1"
    }

    lateinit var cache: SentNotificationsCache


    @Before
    fun init() {
        cache = SentNotificationsCacheImpl(Config(TradeVolumeConfig(TradeVolumeCacheConfig(100, 100),
                NotificationsConfig(100L),
                100)))
    }


    @Test
    fun testAdd() {
        cache.add("", "")
    }
}