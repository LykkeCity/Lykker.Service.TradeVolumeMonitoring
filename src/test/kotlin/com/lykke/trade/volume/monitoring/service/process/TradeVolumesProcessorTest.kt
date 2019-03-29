package com.lykke.trade.volume.monitoring.service.process

import com.lykke.client.accounts.ClientAccountsCache
import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.PersistenceData
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumePersistenceData
import com.lykke.trade.volume.monitoring.service.notification.NotificationService
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesProcessorImpl
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.math.BigDecimal
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class TradeVolumesProcessorTest {

    private lateinit var processor: TradeVolumesProcessor
    private val tradeVolumeCache = TradeVolumeCacheStub()
    private val persistenceManager = PersistenceManagerStub()

    @Mock
    private lateinit var notificationService: NotificationService

    @Mock
    private lateinit var converter: AssetVolumeConverter

    @Mock
    private lateinit var clientAccountsCache: ClientAccountsCache

    @Before
    fun setUp() {
        Mockito.`when`(converter.convert("Asset1", BigDecimal.valueOf(5), "TargetAsset"))
                .thenReturn(BigDecimal.valueOf(50), BigDecimal.valueOf(60))

        whenever(clientAccountsCache.getClientByWalletId(any())).thenAnswer { invocation -> invocation.arguments[0] }

        processor = TradeVolumesProcessorImpl("TargetAsset",
                converter,
                persistenceManager,
                tradeVolumeCache,
                BigDecimal.valueOf(200),
                notificationService,
                clientAccountsCache)
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

        processor.process(EventTradeVolumesWrapper(1234, trades))

        assertEquals(4, tradeVolumeCache.tradeVolumes.size)
        assertCachedVolume(CachedVolume(2, "wallet1", "Asset1", BigDecimal.valueOf(50), trades[2].timestamp),
                tradeVolumeCache.tradeVolumes[0])
        assertCachedVolume(CachedVolume(3, "wallet1", "TargetAsset", BigDecimal.valueOf(15), trades[3].timestamp),
                tradeVolumeCache.tradeVolumes[1])
        assertCachedVolume(CachedVolume(4, "wallet2", "Asset1", BigDecimal.valueOf(60), trades[4].timestamp),
                tradeVolumeCache.tradeVolumes[2])
        assertCachedVolume(CachedVolume(5, "wallet2", "TargetAsset", BigDecimal.valueOf(20), trades[5].timestamp),
                tradeVolumeCache.tradeVolumes[3])

        assertEquals(1, persistenceManager.data.size)
        val persistenceData = persistenceManager.data.single()
        assertEquals(1234, persistenceData.eventSequenceNumber)
        assertEquals(4, persistenceData.tradeVolumes.size)

        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 2, "wallet1", "Asset1", BigDecimal.valueOf(50), trades[2].timestamp), persistenceData.tradeVolumes[0])
        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 3, "wallet1", "TargetAsset", BigDecimal.valueOf(15), trades[3].timestamp), persistenceData.tradeVolumes[1])
        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 4, "wallet2", "Asset1", BigDecimal.valueOf(60), trades[4].timestamp), persistenceData.tradeVolumes[2])
        assertTradeVolumePersistenceData(TradeVolumePersistenceData(1234L, 5, "wallet2", "TargetAsset", BigDecimal.valueOf(20), trades[5].timestamp), persistenceData.tradeVolumes[3])
    }

    @Test
    fun testNotificationSent() {
        val now = Date()
        val trade1 = Date(now.time - 50)
        val trade2 = Date(now.time - 30)
        val trades = listOf(TradeVolume(0, "wallet1", "Asset1", BigDecimal.valueOf(5), trade1),
                TradeVolume(0, "wallet1", "Asset1", BigDecimal.valueOf(105), trade2))

        Mockito.`when`(converter.convert(eq("Asset1"), any(), eq("TargetAsset")))
                .thenAnswer { invocation -> (invocation.arguments[1] as BigDecimal).multiply(BigDecimal.valueOf(2)) }

        processor.process(EventTradeVolumesWrapper(1234, listOf(trades[0])))
        processor.process(EventTradeVolumesWrapper(1235, listOf(trades[1])))

        verify(notificationService).sendTradeVolumeLimitReachedMailNotification(eq("wallet1"), eq("Asset1"), eq(trade2))
    }

    private class TradeVolumeCacheStub : TradeVolumeCache {

        val tradeVolumes = mutableListOf<CachedVolume>()

        override fun add(eventSequenceNumber: Long,
                         tradeIdx: Int,
                         clientId: String,
                         assetId: String,
                         volume: BigDecimal,
                         timestamp: Date): List<Pair<Long, BigDecimal>> {
            tradeVolumes.add(CachedVolume(tradeIdx, clientId, assetId, volume, timestamp))
            var sum = BigDecimal.ZERO
            tradeVolumes.forEach { sum = sum.add(it.targetAssetVolume) }
            return listOf(timestamp.time to sum)
        }

    }

    private class CachedVolume(val tradeIdx: Int,
                               val clientId: String,
                               val assetId: String,
                               val targetAssetVolume: BigDecimal,
                               val timestamp: Date)

    private fun assertCachedVolume(expected: CachedVolume, actual: CachedVolume) {
        assertEquals(expected.tradeIdx, actual.tradeIdx)
        assertEquals(expected.clientId, actual.clientId)
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