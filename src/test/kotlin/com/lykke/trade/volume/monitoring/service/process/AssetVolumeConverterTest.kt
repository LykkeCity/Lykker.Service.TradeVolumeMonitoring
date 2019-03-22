package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.assertEquals
import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.exception.ApplicationException
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder
import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import com.lykke.trade.volume.monitoring.service.process.impl.AssetVolumeConverterImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal

class AssetVolumeConverterTest {

    private lateinit var converter: AssetVolumeConverter

    @Before
    fun setUp() {
        val assetsHolder = Mockito.mock(AssetsHolder::class.java)
        val assetPairsHolder = Mockito.mock(AssetPairsHolder::class.java)
        val pricesHolder = Mockito.mock(PricesHolder::class.java)

        Mockito.`when`(assetsHolder.getAsset("Asset1"))
                .thenReturn(Asset("Asset1", 4))

        Mockito.`when`(assetsHolder.getAsset("Asset2"))
                .thenReturn(Asset("Asset2", 2))

        val assetPair = AssetPair("Pair", "Asset1", "Asset2", 5)
        Mockito.`when`(assetPairsHolder.getAssetPair("Asset1", "Asset2"))
                .thenReturn(assetPair)
        Mockito.`when`(assetPairsHolder.getAssetPair("Asset2", "Asset1"))
                .thenReturn(assetPair)
        Mockito.`when`(assetPairsHolder.getAssetPair("Asset1", "Asset3"))
                .thenReturn(AssetPair("PairWithoutPrice", "Asset1", "Asset3", 5))

        Mockito.`when`(pricesHolder.getPrice("Pair"))
                .thenReturn(BigDecimal("1.55555"))

        converter = AssetVolumeConverterImpl(assetsHolder,
                assetPairsHolder,
                pricesHolder)
    }

    @Test
    fun testConvertBaseToQuoting() {
        assertEquals(BigDecimal("1.56"), converter.convert("Asset1", BigDecimal.ONE, "Asset2"))
        assertEquals(BigDecimal("4.86"), converter.convert("Asset1", BigDecimal("3.1234567"), "Asset2"))
    }

    @Test
    fun testConvertQuotingToBase() {
        assertEquals(BigDecimal("1.0029"), converter.convert("Asset2", BigDecimal("1.56"), "Asset1"))
        assertEquals(BigDecimal("3.1243"), converter.convert("Asset2", BigDecimal("4.86"), "Asset1"))
    }

    @Test
    fun testConvertToSameAsset() {
        assertEquals(BigDecimal.ONE, converter.convert("Asset1", BigDecimal.ONE, "Asset1"))
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownTargetAsset() {
        converter.convert("Asset1", BigDecimal.ONE, "UnknownAsset")
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownAssetPair() {
        converter.convert("Asset3", BigDecimal.ONE, "Asset2")
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownPriceToConvert() {
        converter.convert("Asset1", BigDecimal.ONE, "Asset3")
    }
}