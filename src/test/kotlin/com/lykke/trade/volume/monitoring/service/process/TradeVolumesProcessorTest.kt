package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesProcessorImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

class TradeVolumesProcessorTest {

    private lateinit var processor: TradeVolumesProcessor
    private val tradeVolumeCache = TradeVolumeCacheStub()

    @Before
    fun setUp() {
        val converter = Mockito.mock(AssetVolumeConverter::class.java)
        Mockito.`when`(converter.convert("Asset1", BigDecimal.valueOf(5), "TargetAsset"))
                .thenReturn(BigDecimal.valueOf(50), BigDecimal.valueOf(60))

        processor = TradeVolumesProcessorImpl("TargetAsset", converter, tradeVolumeCache)
    }

    @Test
    fun testProcess() {
        val date = Date()
        val day = TimeUnit.DAYS.toMillis(1)
        val trades = listOf(
                // Unknown asset
                TradeVolume("wallet1", "Asset2", BigDecimal.valueOf(5), date),
                // Unknown price to convert
                TradeVolume("wallet1", "Asset1", BigDecimal.valueOf(6), date),

                TradeVolume("wallet1", "Asset1", BigDecimal.valueOf(5), Date(date.time + day)),
                TradeVolume("wallet1", "TargetAsset", BigDecimal.valueOf(15), Date(date.time + 2 * day)),
                TradeVolume("wallet2", "Asset1", BigDecimal.valueOf(5), Date(date.time + 3 * day)),
                TradeVolume("wallet2", "TargetAsset", BigDecimal.valueOf(20), Date(date.time + 4 * day))
        )

        processor.process(EventTradeVolumesWrapper("MessageId",
                trades))

        assertEquals(4, tradeVolumeCache.trades.size)
        assertCachedVolume(CachedVolume("wallet1", "Asset1", BigDecimal.valueOf(50), trades[2].timestamp),
                tradeVolumeCache.trades[0])
        assertCachedVolume(CachedVolume("wallet1", "TargetAsset", BigDecimal.valueOf(15), trades[3].timestamp),
                tradeVolumeCache.trades[1])
        assertCachedVolume(CachedVolume("wallet2", "Asset1", BigDecimal.valueOf(60), trades[4].timestamp),
                tradeVolumeCache.trades[2])
        assertCachedVolume(CachedVolume("wallet2", "TargetAsset", BigDecimal.valueOf(20), trades[5].timestamp),
                tradeVolumeCache.trades[3])
    }

    private class TradeVolumeCacheStub : TradeVolumeCache {

        val trades = mutableListOf<CachedVolume>()

        override fun clear() {
        }

        override fun add(walletId: String, assetId: String, volume: BigDecimal, timestamp: Date): BigDecimal {
            trades.add(CachedVolume(walletId, assetId, volume, timestamp))
            return BigDecimal.ZERO
        }

        override fun get(walletId: String, assetId: String): BigDecimal {
            return BigDecimal.ZERO
        }

    }

    private class CachedVolume(val walletId: String,
                               val assetId: String,
                               val targetAssetVolume: BigDecimal,
                               val timestamp: Date)

    private fun assertCachedVolume(expected: CachedVolume, actual: CachedVolume) {
        assertEquals(expected.walletId, actual.walletId)
        assertEquals(expected.assetId, actual.assetId)
        assertEquals(expected.targetAssetVolume, actual.targetAssetVolume)
        assertEquals(expected.timestamp, actual.timestamp)
    }
}