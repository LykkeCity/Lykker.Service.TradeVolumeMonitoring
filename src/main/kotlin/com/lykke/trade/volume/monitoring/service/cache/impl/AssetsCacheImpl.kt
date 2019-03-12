package com.lykke.trade.volume.monitoring.service.cache.impl

import com.lykke.trade.volume.monitoring.service.cache.AssetsCache
import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.loader.AssetsLoader
import com.lykke.utils.logging.ThrottlingLogger

class AssetsCacheImpl(private val loader: AssetsLoader,
                      override val updateInterval: Long) : AssetsCache {

    companion object {
        private val LOGGER = ThrottlingLogger.getLogger(AssetsCacheImpl::class.java.name)
    }

    private var assetsById: MutableMap<String, Asset> = HashMap()

    override fun getAsset(assetId: String): Asset? {
        var asset = assetsById[assetId]
        if (asset == null) {
            asset = loader.loadAsset(assetId)?.let {
                assetsById[assetId] = it
                it
            }
        }
        return asset
    }

    override fun update() {
        val assetsById = try {
            loader.loadAssetsByIdMap()
        } catch (e: Exception) {
            LOGGER.error("Unable to load assets", e)
            return
        }
        this.assetsById = assetsById.toMutableMap()
        LOGGER.debug("Loaded ${assetsById.size} assets")
    }

    init {
        update()
        LOGGER.info("Loaded ${assetsById.size} assets initially")
    }
}