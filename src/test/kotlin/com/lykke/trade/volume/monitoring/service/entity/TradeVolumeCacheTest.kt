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

class TradeVolumeCacheTest {

    private lateinit var tradeVolumeCache: TradeVolumeCache
    private val WALLET1 = "WALLET1!"
    private val ASSET1 = "ASSET1"

    @Before
    fun init() {
        tradeVolumeCache = TradeVolumeCacheImpl(Config(TradeVolumeConfig(TradeVolumeCacheConfig(100L, 100))))
    }

    @Test
    fun testAddNewVolume() {
        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        assertEquals(BigDecimal.valueOf(1.0), tradeVolumeCache.get(WALLET1, ASSET1))

        val result = tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(10), Date())
        assertEquals(BigDecimal.valueOf(11), result)
    }

    @Test
    fun testNotAllExpired() {
        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        Thread.sleep(50)
        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        assertEquals(BigDecimal.valueOf(2.0), tradeVolumeCache.get(WALLET1, ASSET1))
        Thread.sleep(70)
        assertEquals(BigDecimal.valueOf(1.0), tradeVolumeCache.get(WALLET1, ASSET1))
    }

    @Test
    fun testAllExpired() {
        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        Thread.sleep(150)
        assertEquals(BigDecimal.ZERO, tradeVolumeCache.get(WALLET1, ASSET1))

        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        assertEquals(BigDecimal.valueOf(1.0), tradeVolumeCache.get(WALLET1, ASSET1))
    }

    @Test
    fun testClear() {
        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        Thread.sleep(150)
        assertEquals(BigDecimal.ZERO, tradeVolumeCache.get(WALLET1, ASSET1))

        tradeVolumeCache.add(WALLET1, ASSET1, BigDecimal.valueOf(1.0), Date())
        assertEquals(BigDecimal.valueOf(1.0), tradeVolumeCache.get(WALLET1, ASSET1))
    }
}