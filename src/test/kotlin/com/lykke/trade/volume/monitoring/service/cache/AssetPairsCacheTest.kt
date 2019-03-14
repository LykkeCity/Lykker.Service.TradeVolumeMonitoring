package com.lykke.trade.volume.monitoring.service.cache

import com.lykke.trade.volume.monitoring.service.cache.impl.AssetPairsCacheImpl
import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class AssetPairsCacheTest {

    private lateinit var assetPairsCache: AssetPairsCache

    @Before
    fun setUp() {
        val loader = Mockito.mock(AssetPairsLoader::class.java)
        Mockito.`when`(loader.loadAssetPairsByIdMap())
                .thenReturn(mapOf(
                        Pair("Pair1", AssetPair("Pair1", "Asset1", "Asset2", 0)),
                        Pair("Pair2", AssetPair("Pair2", "Asset1", "Asset3", 1)),
                        Pair("Pair2_duplicate", AssetPair("Pair2_duplicate", "Asset3", "Asset1", 2)),
                        Pair("WrongKey", AssetPair("Pair3", "Asset2", "Asset3", 3))
                ))

        assetPairsCache = AssetPairsCacheImpl(loader, 0L)
    }

    @Test
    fun getAssetPair() {
        val assetPair1 = assetPairsCache.getAssetPair("Asset1", "Asset2")
        val assetPair2 = assetPairsCache.getAssetPair("Asset2", "Asset1")

        assertNotNull(assetPair1)
        assertEquals("Pair1", assetPair1!!.id)
        assertEquals("Asset1", assetPair1.baseAssetId)
        assertEquals("Asset2", assetPair1.quotingAssetId)
        assertEquals(0, assetPair1.accuracy)
        assertTrue(assetPair1 === assetPair2)
    }

    @Test
    fun getAssetPairWithWrongKey() {
        val assetPair = assetPairsCache.getAssetPair("Asset2", "Asset3")

        assertNotNull(assetPair)
        assertEquals("Pair3", assetPair!!.id)
        assertEquals("Asset2", assetPair.baseAssetId)
        assertEquals("Asset3", assetPair.quotingAssetId)
        assertEquals(3, assetPair.accuracy)
    }

    @Test
    fun getDuplicatedAssetPair() {
        val assetPair1 = assetPairsCache.getAssetPair("Asset1", "Asset3")
        val assetPair2 = assetPairsCache.getAssetPair("Asset3", "Asset1")

        assertNotNull(assetPair1)
        assertEquals("Pair2", assetPair1!!.id)
        assertEquals("Asset1", assetPair1.baseAssetId)
        assertEquals("Asset3", assetPair1.quotingAssetId)
        assertEquals(1, assetPair1.accuracy)
        assertTrue(assetPair1 === assetPair2)
    }
}