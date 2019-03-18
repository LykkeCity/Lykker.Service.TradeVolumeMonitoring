package com.lykke.trade.volume.monitoring.service.entity

import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeCacheConfig
import com.lykke.trade.volume.monitoring.service.config.TradeVolumeConfig
import com.lykke.trade.volume.monitoring.service.entity.impl.TradeVolumeCacheImpl
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals

class TradeVolumeCacheTest {

    private lateinit var tradeVolumeCache: TradeVolumeCache
    private val CLIENT1 = "CLIENT1"
    private val ASSET1 = "ASSET1"

    @Before
    fun init() {
        tradeVolumeCache = TradeVolumeCacheImpl(Config(TradeVolumeConfig(TradeVolumeCacheConfig(100L, 2, 100L), BigDecimal.valueOf(1000))))
    }

    @Test
    fun volumesAreNotifiedOnlyOnce() {

    }

    @Test
    fun oneVolumeExceedsLimit() {
        val timestamp = Date()
        val volumesByTimestamp = tradeVolumeCache.add(CLIENT1, ASSET1, BigDecimal.valueOf(1000), timestamp)
        assertEquals(1, volumesByTimestamp.size)
        assertEquals(BigDecimal.valueOf(1000), volumesByTimestamp[timestamp.time])
    }

    @Test
    fun testVolumeIsInsertedAtTheMiddle() {
        val now = Date()
        val first = Date(now.time - 100)
        val second = Date(first.time + 99)
        val third = Date(second.time + 99)

        val firstResult = tradeVolumeCache.add(CLIENT1, ASSET1, BigDecimal.valueOf(10), first)
        assertEquals(0, firstResult.size)
        val thirdResult = tradeVolumeCache.add(CLIENT1, ASSET1, BigDecimal.valueOf(900), third)
        assertEquals(0, thirdResult.size)

        val secondResult = tradeVolumeCache.add(CLIENT1, ASSET1, BigDecimal.valueOf(995), second)

        assertEquals(2, secondResult.size)
        assertEquals(BigDecimal.valueOf(1005) , secondResult[second.time])
        assertEquals(BigDecimal.valueOf(1895) , secondResult[third.time])
    }

    @Test
    fun testCacheClean() {

    }

    @Test
    fun lockIsRemovedIfDuringCleanAllVolumesRemoved() {

    }

    @Test
    fun testAddNewVolume() {

    }

    @Test
    fun testNotAllExpired() {

    }

    @Test
    fun testAllExpired() {

    }


}