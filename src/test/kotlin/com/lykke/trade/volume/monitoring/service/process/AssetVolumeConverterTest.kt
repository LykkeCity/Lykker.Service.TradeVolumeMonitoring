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
    private val assetPairsHolder = Mockito.mock(AssetPairsHolder::class.java)
    private val pricesHolder = Mockito.mock(PricesHolder::class.java)
    private val assetsHolder = Mockito.mock(AssetsHolder::class.java)

    @Before
    fun setUp() {
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
        assertEquals(BigDecimal("1.56"), converter.convert("Asset1", BigDecimal.ONE, emptySet(), "Asset2"))
        assertEquals(BigDecimal("4.86"), converter.convert("Asset1", BigDecimal("3.1234567"), emptySet(), "Asset2"))
    }

    @Test
    fun testConvertQuotingToBase() {
        assertEquals(BigDecimal("1.0029"), converter.convert("Asset2", BigDecimal("1.56"), emptySet(), "Asset1"))
        assertEquals(BigDecimal("3.1243"), converter.convert("Asset2", BigDecimal("4.86"), emptySet(), "Asset1"))
    }

    @Test
    fun testConvertToSameAsset() {
        assertEquals(BigDecimal.ONE, converter.convert("Asset1", BigDecimal.ONE, emptySet(), "Asset1"))
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownTargetAsset() {
        converter.convert("Asset1", BigDecimal.ONE, emptySet(), "UnknownAsset")
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownAssetPair() {
        converter.convert("Asset3", BigDecimal.ONE, emptySet(), "Asset2")
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownStraightAndCrossAssetPairs() {
        converter.convert("Asset3", BigDecimal.ONE, setOf("CrossAsset1", "CrossAsset2", "CrossAsset3"), "Asset2")
    }

    @Test
    fun testConvertUnknownAssetPairThroughCrossAsset() {
        Mockito.`when`(assetPairsHolder.getAssetPair("Asset3", "CrossAsset2"))
                .thenReturn(AssetPair("CrossPair2-1", "Asset3", "CrossAsset2", 1))
        Mockito.`when`(assetPairsHolder.getAssetPair("CrossAsset2", "Asset2"))
                .thenReturn(AssetPair("CrossPair2-2-WithoutPrice", "Asset2", "CrossAsset2", 2))

        Mockito.`when`(assetPairsHolder.getAssetPair("Asset3", "CrossAsset3"))
                .thenReturn(AssetPair("CrossPair3-1", "Asset3", "CrossAsset3", 3))
        Mockito.`when`(assetPairsHolder.getAssetPair("CrossAsset3", "Asset2"))
                .thenReturn(AssetPair("CrossPair3-2", "Asset2", "CrossAsset3", 4))

        Mockito.`when`(pricesHolder.getPrice("CrossPair2-1"))
                .thenReturn(BigDecimal("1.1"))
        Mockito.`when`(pricesHolder.getPrice("CrossPair3-1"))
                .thenReturn(BigDecimal("10.123"))
        Mockito.`when`(pricesHolder.getPrice("CrossPair3-2"))
                .thenReturn(BigDecimal("15.6789"))

        val result = converter.convert("Asset3", BigDecimal("1.23"), setOf("CrossAsset1", "CrossAsset2", "CrossAsset3"), "Asset2")
        assertEquals(BigDecimal("0.79"), result)
    }

    @Test(expected = ApplicationException::class)
    fun testUnknownPriceToConvert() {
        converter.convert("Asset1", BigDecimal.ONE, emptySet(), "Asset3")
    }

    @Test
    fun testConvertUnknownPriceThroughCrossAsset() {
        Mockito.`when`(assetsHolder.getAsset("Asset3"))
                .thenReturn(Asset("Asset3", 3))

        Mockito.`when`(assetPairsHolder.getAssetPair("Asset1", "CrossAsset2"))
                .thenReturn(AssetPair("CrossPair2-1", "Asset1", "CrossAsset2", 1))
        Mockito.`when`(assetPairsHolder.getAssetPair("CrossAsset2", "Asset3"))
                .thenReturn(AssetPair("CrossPair2-2-WithoutPrice", "Asset3", "CrossAsset2", 2))

        Mockito.`when`(assetPairsHolder.getAssetPair("Asset1", "CrossAsset3"))
                .thenReturn(AssetPair("CrossPair3-1", "Asset1", "CrossAsset3", 3))
        Mockito.`when`(assetPairsHolder.getAssetPair("CrossAsset3", "Asset3"))
                .thenReturn(AssetPair("CrossPair3-2", "Asset3", "CrossAsset3", 4))

        Mockito.`when`(pricesHolder.getPrice("CrossPair2-1"))
                .thenReturn(BigDecimal("1.1"))
        Mockito.`when`(pricesHolder.getPrice("CrossPair3-1"))
                .thenReturn(BigDecimal("10.123"))
        Mockito.`when`(pricesHolder.getPrice("CrossPair3-2"))
                .thenReturn(BigDecimal("15.6789"))

        val result = converter.convert("Asset1", BigDecimal("1.23"), setOf("CrossAsset1", "CrossAsset2", "CrossAsset3"), "Asset3")
        assertEquals(BigDecimal("0.794"), result)
    }
}