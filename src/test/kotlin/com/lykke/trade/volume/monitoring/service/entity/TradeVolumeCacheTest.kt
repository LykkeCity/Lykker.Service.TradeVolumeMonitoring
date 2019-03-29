package com.lykke.trade.volume.monitoring.service.entity

import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.cache.impl.TradeVolumeCacheImpl
import com.lykke.trade.volume.monitoring.service.getConfig
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal
import java.util.Date
import kotlin.test.assertEquals

class TradeVolumeCacheTest {

    private lateinit var tradeVolumeCache: TradeVolumeCache
    private var loader = Mockito.mock(EventsLoader::class.java)
    private val CLIENT1 = "CLIENT1"
    private val CLIENT2 = "CLIENT2"

    private val ASSET1 = "ASSET1"
    private val ASSET2 = "ASSET2"

    @Before
    fun init() {
        tradeVolumeCache = TradeVolumeCacheImpl(getConfig().tradeVolumeConfig.tradeVolumeCacheConfig, loader)
    }

    @Test
    fun testOneVolume() {
        val timestamp = Date()
        val volumesByTimestamp = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(1000), timestamp)
        assertEquals(1, volumesByTimestamp.size)
        assertEquals(BigDecimal.valueOf(1000), volumesByTimestamp.first().second)
    }

    @Test
    fun testVolumeIsInsertedAtTheMiddle() {
        val now = Date()
        val firstTimestamp = Date(now.time - 100)
        val secondTimestamp = Date(firstTimestamp.time + 99)
        val thirdTimestamp = Date(secondTimestamp.time + 99)

        val firstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstTimestamp)
        assertEquals(1, firstResult.size)
        assertEquals(BigDecimal.valueOf(10), firstResult.first().second)
        val thirdResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(900), thirdTimestamp)
        assertEquals(1, thirdResult.size)
        assertEquals(BigDecimal.valueOf(900), thirdResult.first().second)

        val secondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(995), secondTimestamp)

        assertEquals(2, secondResult.size)
        assertEquals(BigDecimal.valueOf(1005), secondResult.findLast { pair -> pair.first == secondTimestamp.time }!!.second)
        assertEquals(BigDecimal.valueOf(1895), secondResult.findLast { pair -> pair.first == thirdTimestamp.time }!!.second)
    }

    @Test
    fun testVolumeIsInsertedSequentially() {
        val now = Date()
        val firstTimestamp = Date(now.time - 100)
        val secondTimestamp = Date(firstTimestamp.time + 99)
        val thirdTimestamp = Date(secondTimestamp.time + 99)

        val firstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstTimestamp)
        assertEquals(1, firstResult.size)

        val secondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(995), secondTimestamp)
        assertEquals(1, secondResult.size)
        assertEquals(BigDecimal.valueOf(1005), secondResult.findLast { pair -> pair.first == secondTimestamp.time }!!.second)

        val thirdResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(900), thirdTimestamp)
        assertEquals(1, thirdResult.size)
        assertEquals(BigDecimal.valueOf(1895), thirdResult.first().second)

    }

    @Test
    fun testInsertVolumeAtTheEnd() {
        val now = Date()
        val firstTimestamp = Date(now.time - 100)
        val secondTimestamp = Date(firstTimestamp.time + 99)
        val thirdTimestamp = Date(secondTimestamp.time + 99)

        val secondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(995), secondTimestamp)
        assertEquals(1, secondResult.size)
        assertEquals(secondTimestamp.time, secondResult.first().first)
        assertEquals(BigDecimal.valueOf(995), secondResult.first().second)

        val thirdResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(900), thirdTimestamp)
        assertEquals(1, thirdResult.size)
        assertEquals(thirdTimestamp.time, thirdResult.first().first)
        assertEquals(BigDecimal.valueOf(1895), thirdResult.first().second)

        val firstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstTimestamp)
        assertEquals(3, firstResult.size)

        assertEquals(BigDecimal.valueOf(10), firstResult.findLast { pair -> pair.first == firstTimestamp.time }!!.second)
        assertEquals(BigDecimal.valueOf(1005), firstResult.findLast { pair -> pair.first == secondTimestamp.time }!!.second)
        assertEquals(BigDecimal.valueOf(1895), firstResult.findLast { pair -> pair.first == thirdTimestamp.time }!!.second)
    }

    @Test
    fun severalClientsInCache() {
        val now = Date()
        val firstClientFirstTimestamp = Date(now.time - 100)
        val firstClientSecondTimestamp = Date(firstClientFirstTimestamp.time + 99)
        val firstClientThirdTimestamp = Date(firstClientSecondTimestamp.time + 99)

        val secondClientFirstTimeStamp = Date(now.time - 10)
        val secondClientSecondTimeStamp = Date(now.time - 5)
        val secondClientThirdTimestamp = Date(now.time - 15)

        val secondClientFirstResult = tradeVolumeCache.add(1L, 1, CLIENT2, ASSET1, BigDecimal.valueOf(200), secondClientFirstTimeStamp)
        assertEquals(1, secondClientFirstResult.size)
        assertEquals(BigDecimal.valueOf(200), secondClientFirstResult.first().second)

        val secondClientSecondResult = tradeVolumeCache.add(1L, 1, CLIENT2, ASSET1, BigDecimal.valueOf(88), secondClientSecondTimeStamp)
        assertEquals(1, secondClientSecondResult.size)
        assertEquals(BigDecimal.valueOf(288), secondClientSecondResult.first().second)

        val secondClientThirdResult = tradeVolumeCache.add(1L, 1, CLIENT2, ASSET1, BigDecimal.valueOf(150), secondClientThirdTimestamp)
        assertEquals(3, secondClientThirdResult.size)
        assertEquals(BigDecimal.valueOf(150), secondClientThirdResult.findLast { pair -> pair.first == secondClientThirdTimestamp.time }!!.second)
        assertEquals(BigDecimal.valueOf(438), secondClientThirdResult.findLast { pair -> pair.first == secondClientSecondTimeStamp.time }!!.second)
        assertEquals(BigDecimal.valueOf(350), secondClientThirdResult.findLast { pair -> pair.first == secondClientFirstTimeStamp.time }!!.second)

        val firstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstClientFirstTimestamp)
        assertEquals(1, firstResult.size)

        val secondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(995), firstClientSecondTimestamp)
        assertEquals(1, secondResult.size)
        assertEquals(BigDecimal.valueOf(1005), secondResult.findLast { pair -> pair.first == firstClientSecondTimestamp.time }!!.second)

        val thirdResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(900), firstClientThirdTimestamp)
        assertEquals(1, thirdResult.size)
        assertEquals(BigDecimal.valueOf(1895), thirdResult.first().second)
    }

    @Test
    fun testSeveralAssetsForOneClient() {
        val now = Date()
        val firstAssetFirstTimestamp = Date(now.time - 60)
        val firstAssetSecondTimestamp = Date(now.time - 50)

        val secondAssetFirstTimestamp = Date(now.time - 55)
        val secondAssetSecondTimestamp = Date(now.time - 45)

        val firstAssetFirstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstAssetFirstTimestamp)
        assertEquals(1, firstAssetFirstResult.size)
        val firstAssetSecondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstAssetSecondTimestamp)
        assertEquals(1, firstAssetSecondResult.size)


        val secondAssetFirstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET2, BigDecimal.valueOf(30), secondAssetFirstTimestamp)
        assertEquals(1, secondAssetFirstResult.size)
        assertEquals(secondAssetFirstTimestamp.time, secondAssetFirstResult.first().first)
        assertEquals(BigDecimal.valueOf(30), secondAssetFirstResult.first().second)

        val secondAssetSecondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET2, BigDecimal.valueOf(50), secondAssetSecondTimestamp)
        assertEquals(1, secondAssetSecondResult.size)
        assertEquals(secondAssetSecondTimestamp.time, secondAssetSecondResult.first().first)
        assertEquals(BigDecimal.valueOf(80), secondAssetSecondResult.first().second)
    }

    @Test
    fun testClientHasSeveralTradesAtSameTimestamp() {
        val now = Date()
        val firstTimestamp = Date(now.time - 10)
        val secondTimestamp = Date(now.time)

        tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), firstTimestamp)

        val resultSameTimestamp = tradeVolumeCache.add(1L, 2, CLIENT1, ASSET1, BigDecimal.valueOf(20), firstTimestamp)
        assertEquals(1, resultSameTimestamp.size)
        assertEquals(firstTimestamp.time, resultSameTimestamp.first().first)

        assertEquals(BigDecimal.valueOf(30), resultSameTimestamp.first().second)

        val resultSecondTimestamp = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(5), secondTimestamp)
        assertEquals(1, resultSecondTimestamp.size)
        assertEquals(secondTimestamp.time, resultSecondTimestamp.first().first)
        assertEquals(BigDecimal.valueOf(35), resultSecondTimestamp.first().second)
    }

    @Test
    fun testCacheClean() {
        val now = Date()
        tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), Date(now.time - 210))
        tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(100), Date(now.time - 250))
        val cleanCacheMethod = tradeVolumeCache::class.java.getDeclaredMethod("cleanCache")
        cleanCacheMethod.isAccessible = true
        cleanCacheMethod.invoke(tradeVolumeCache)

        val firstResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(20), Date(now.time - 300))
        assertEquals(1, firstResult.size)
        assertEquals(BigDecimal.valueOf(20), firstResult.first().second)

        val secondResult = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(30), Date(now.time - 220))
        assertEquals(1, secondResult.size)
        assertEquals(BigDecimal.valueOf(50), secondResult.first().second)
    }

    @Test
    fun testNotAllExpired() {
        val now = Date()
        tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(10), Date(now.time - 100))
        tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(100), Date(now.time - 250))
        val cleanCacheMethod = tradeVolumeCache::class.java.getDeclaredMethod("cleanCache")
        cleanCacheMethod.isAccessible = true
        cleanCacheMethod.invoke(tradeVolumeCache)

        val result = tradeVolumeCache.add(1L, 1, CLIENT1, ASSET1, BigDecimal.valueOf(30), Date(now.time - 10))
        assertEquals(1, result.size)
        assertEquals(BigDecimal.valueOf(40), result.first().second)
    }

    @Test
    fun testNotAllExpiredTradeInsertedAtTheEnd() {
        val now = Date()
        val oldDate = Date(now.time - 300)
        val date1 = Date(now.time - 3)
        val date2 = Date(now.time - 2)

        tradeVolumeCache.add(3, 3, CLIENT1, ASSET1, BigDecimal("2"), date2)
        tradeVolumeCache.add(1, 1, CLIENT1, ASSET1, BigDecimal("1000"), oldDate)

        val cleanCacheMethod = tradeVolumeCache::class.java.getDeclaredMethod("cleanCache")
        cleanCacheMethod.isAccessible = true
        cleanCacheMethod.invoke(tradeVolumeCache)

        val result = tradeVolumeCache.add(2, 2, CLIENT1, ASSET1, BigDecimal("1"), date1)

        assertEquals(BigDecimal("3"), result.single { it.first == date2.time }.second)
    }

    @Test
    fun testInitialization() {
        val date = Date()
        val date1 = Date(date.time - 1)
        val date2 = Date(date.time - 2)
        val date3 = Date(date.time - 3)
        val oldDate = Date(date.time - 300)
        Mockito.`when`(loader.loadEvents())
                .thenReturn(listOf(
                        EventPersistenceData(1L, date, listOf(
                                TradeVolumePersistenceData(0, CLIENT1, ASSET1, BigDecimal("1.1"), date1),
                                TradeVolumePersistenceData(0, CLIENT1, ASSET1, BigDecimal("1.1"), oldDate),
                                TradeVolumePersistenceData(1, CLIENT1, ASSET1, BigDecimal("2.22"), date2),
                                TradeVolumePersistenceData(2, CLIENT2, ASSET1, BigDecimal("2.22"), date2)
                        )),
                        EventPersistenceData(2L, date3, listOf(
                                TradeVolumePersistenceData(0, CLIENT1, ASSET1, BigDecimal("3.333"), date3),
                                TradeVolumePersistenceData(1, CLIENT1, ASSET2, BigDecimal("3.333"), date3)
                        ))
                ))
        tradeVolumeCache = TradeVolumeCacheImpl(getConfig().tradeVolumeConfig.tradeVolumeCacheConfig, loader)
        (tradeVolumeCache as TradeVolumeCacheImpl).init()

        val date4 = Date(date.time - 4)
        val result = tradeVolumeCache.add(3L, 0, CLIENT1, ASSET1, BigDecimal("4.4444"), date4)

        assertEquals(4, result.size)
        assertEquals(BigDecimal("11.0974"), result.single { it.first == date1.time }.second)
        assertEquals(BigDecimal("9.9974"), result.single { it.first == date2.time }.second)
        assertEquals(BigDecimal("7.7774"), result.single { it.first == date3.time }.second)
        assertEquals(BigDecimal("4.4444"), result.single { it.first == date4.time }.second)

    }

}