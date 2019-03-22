package com.lykke.trade.volume.monitoring.service.cache

import com.lykke.trade.volume.monitoring.service.cache.impl.PricesCacheImpl
import com.lykke.trade.volume.monitoring.service.entity.Rate
import com.lykke.trade.volume.monitoring.service.loader.RatesLoader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import java.math.BigDecimal
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class PricesCacheTest(private val data: Data) {

    companion object {
        data class Data(val assetPairId: String,
                        val expectedPrice: BigDecimal?)

        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Data> {
            return listOf(
                    Data("PairWithoutMidPrice1", null),
                    Data("PairWithoutMidPrice2", null),
                    Data("PairWithoutMidPrice3", null),
                    Data("PairWithoutMidPrice4", null),
                    Data("PairWithoutMidPrice5", null),
                    Data("PairWithoutMidPrice6", null),
                    Data("Pair1", BigDecimal("10.5505")),
                    Data("NewPair", BigDecimal("20.025"))
            )
        }
    }

    private lateinit var pricesCache: PricesCache

    @Before
    fun setUp() {
        val loader = Mockito.mock(RatesLoader::class.java)
        Mockito.`when`(loader.loadRatesByAssetPairIdMap())
                .thenReturn(mapOf(
                        Pair("PairWithoutMidPrice1", Rate("PairWithoutMidPrice1", null, null)),
                        Pair("PairWithoutMidPrice2", Rate("PairWithoutMidPrice2", null, BigDecimal.ZERO)),
                        Pair("PairWithoutMidPrice3", Rate("PairWithoutMidPrice3", BigDecimal.ZERO, null)),
                        Pair("PairWithoutMidPrice4", Rate("PairWithoutMidPrice4", BigDecimal.ONE, BigDecimal.ZERO)),
                        Pair("PairWithoutMidPrice5", Rate("PairWithoutMidPrice5", -BigDecimal.ONE, BigDecimal.ONE)),
                        Pair("PairWithoutMidPrice6", Rate("PairWithoutMidPrice6", BigDecimal.ONE, -BigDecimal.ONE)),
                        Pair("Pair1", Rate("Pair1", BigDecimal("10.1"), BigDecimal("11.001"))))
                )

        Mockito.`when`(loader.loadRate("NewPair"))
                .thenReturn(Rate("NewPair", BigDecimal("10.0"), BigDecimal("30.05")))

        pricesCache = PricesCacheImpl(loader, 0L)
    }

    @Test
    fun testGetPrice() {
        val price = pricesCache.getPrice(data.assetPairId)
        assertEquals(data.expectedPrice, price)
    }
}