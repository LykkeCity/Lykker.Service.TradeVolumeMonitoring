package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.PersistenceData
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumePersistenceData
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
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
    private val persistenceManager = PersistenceManagerStub()

    @Before
    fun setUp() {
        val converter = Mockito.mock(AssetVolumeConverter::class.java)
        Mockito.`when`(converter.convert("Asset1", BigDecimal.valueOf(5), "TargetAsset"))
                .thenReturn(BigDecimal.valueOf(50), BigDecimal.valueOf(60))

        processor = TradeVolumesProcessorImpl("TargetAsset",
                converter,
                persistenceManager,
                tradeVolumeCache)
    }

    @Test
    fun testProcess() {
        val date = Date()
        val day = TimeUnit.DAYS.toMillis(1)
        val trades = listOf(
                // Unknown asset
                TradeVolume(0, "wallet1", "Asset2", BigDecimal.valueOf(5), date),
                // Unknown price to convert
                TradeVolume(1, "wallet1", "Asset1", BigDecimal.valueOf(6), date),

                TradeVolume(2, "wallet1", "Asset1", BigDecimal.valueOf(5), Date(date.time + day)),
                TradeVolume(3, "wallet1", "TargetAsset", BigDecimal.valueOf(15), Date(date.time + 2 * day)),
                TradeVolume(4, "wallet2", "Asset1", BigDecimal.valueOf(5), Date(date.time + 3 * day)),
                TradeVolume(5, "wallet2", "TargetAsset", BigDecimal.valueOf(20), Date(date.time + 4 * day))
        )

        processor.process(EventTradeVolumesWrapper("1234", trades))

        assertEquals(4, tradeVolumeCache.tradeVolumes.size)
        assertCachedVolume(CachedVolume("wallet1", "Asset1", BigDecimal.valueOf(50), trades[2].timestamp),
                tradeVolumeCache.tradeVolumes[0])
        assertCachedVolume(CachedVolume("wallet1", "TargetAsset", BigDecimal.valueOf(15), trades[3].timestamp),
                tradeVolumeCache.tradeVolumes[1])
        assertCachedVolume(CachedVolume("wallet2", "Asset1", BigDecimal.valueOf(60), trades[4].timestamp),
                tradeVolumeCache.tradeVolumes[2])
        assertCachedVolume(CachedVolume("wallet2", "TargetAsset", BigDecimal.valueOf(20), trades[5].timestamp),
                tradeVolumeCache.tradeVolumes[3])

        assertEquals(1, persistenceManager.data.size)
        val persistenceData = persistenceManager.data.single()
        assertEquals("1234", persistenceData.eventId)
        assertEquals(4, persistenceData.tradeVolumes.size)

        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 2, "wallet1", "Asset1", BigDecimal.valueOf(50), trades[2].timestamp), persistenceData.tradeVolumes[0])
        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 3, "wallet1", "TargetAsset", BigDecimal.valueOf(15), trades[3].timestamp), persistenceData.tradeVolumes[1])
        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 4, "wallet2", "Asset1", BigDecimal.valueOf(60), trades[4].timestamp), persistenceData.tradeVolumes[2])
        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 5, "wallet2", "TargetAsset", BigDecimal.valueOf(20), trades[5].timestamp), persistenceData.tradeVolumes[3])
    }

    private class TradeVolumeCacheStub : TradeVolumeCache {

        val tradeVolumes = mutableListOf<CachedVolume>()

        override fun clear() {
        }

        override fun add(walletId: String, assetId: String, volume: BigDecimal, timestamp: Date): BigDecimal {
            tradeVolumes.add(CachedVolume(walletId, assetId, volume, timestamp))
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

    private fun assertTradeVolumePersistenceData(expected: TradeVolumePersistenceData,
                                                 actual: TradeVolumePersistenceData) {
        assertEquals(expected.clientId, actual.clientId)
        assertEquals(expected.assetId, actual.assetId)
        assertEquals(expected.targetAssetVolume, actual.targetAssetVolume)
        assertEquals(expected.timestamp, actual.timestamp)
    }

    private class PersistenceManagerStub : PersistenceManager {
        val data = mutableListOf<PersistenceData>()
        override fun persist(data: PersistenceData) {
            this.data.add(data)
        }
    }
}