package com.lykke.trade.volume.monitoring.service.holder

import com.lykke.trade.volume.monitoring.service.cache.PricesCache
import com.lykke.trade.volume.monitoring.service.holder.impl.PricesHolderImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PricesHolderTest {

    private lateinit var holder: PricesHolder

    @Before
    fun setUp() {
        val pricesCache = Mockito.mock(PricesCache::class.java)
        Mockito.`when`(pricesCache.getPrice("Pair"))
                .thenReturn(BigDecimal.ONE)

        holder = PricesHolderImpl(pricesCache)
    }

    @Test
    fun testGetPrice() {
        assertNull(holder.getPrice("UnknownAsset"))
        assertEquals(BigDecimal.ONE, holder.getPrice("Pair"))
    }

}