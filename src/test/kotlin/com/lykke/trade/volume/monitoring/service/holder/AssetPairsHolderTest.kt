package com.lykke.trade.volume.monitoring.service.holder

import com.lykke.trade.volume.monitoring.service.cache.AssetPairsCache
import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.holder.impl.AssetPairsHolderImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AssetPairsHolderTest {

    private lateinit var holder: AssetPairsHolder

    @Before
    fun setUp() {
        val assetPairsCache = Mockito.mock(AssetPairsCache::class.java)
        Mockito.`when`(assetPairsCache.getAssetPair("Asset1", "Asset2"))
                .thenReturn(AssetPair("AssetPair1", "Asset1", "Asset2", 0))

        holder = AssetPairsHolderImpl(assetPairsCache)
    }

    @Test
    fun testGetAssetPair() {
        assertNull(holder.getAssetPair("Asset1", "Asset3"))
        assertNull(holder.getAssetPair("Asset3", "Asset2"))
        assertNull(holder.getAssetPair("Asset3", "Asset2"))
        assertNull(holder.getAssetPair("Asset1", "Asset1"))
        assertNotNull(holder.getAssetPair("Asset1", "Asset2"))
    }

}