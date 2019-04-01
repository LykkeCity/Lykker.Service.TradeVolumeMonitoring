package com.lykke.trade.volume.monitoring.service.cache.impl

import com.lykke.trade.volume.monitoring.service.cache.AssetsCache
import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.loader.AssetsLoader
import com.lykke.utils.logging.MetricsLogger
import com.lykke.utils.logging.ThrottlingLogger
import java.util.concurrent.ConcurrentHashMap

class AssetsCacheImpl(private val loader: AssetsLoader,
                      override val updateInterval: Long) : AssetsCache {

    companion object {
        private val LOGGER = ThrottlingLogger.getLogger(AssetsCacheImpl::class.java.name)
        private val METRICS_LOGGER = MetricsLogger.getLogger()
    }

    @Volatile
    private var assetsById = ConcurrentHashMap<String, Asset>()

    override fun getAsset(assetId: String): Asset? {
        var asset = assetsById[assetId]
        if (asset == null) {
            asset = loader.loadAsset(assetId)
            if (asset != null) {
                assetsById[assetId] = asset
            }
        }
        return asset
    }

    override fun update() {
        val assetsById = try {
            loader.loadAssetsByIdMap()
        } catch (e: Exception) {
            val message = "Unable to load assets"
            LOGGER.error(message, e)
            METRICS_LOGGER.logError(message, e)
            return
        }
        this.assetsById = ConcurrentHashMap(assetsById)
        LOGGER.debug("Loaded ${assetsById.size} assets")
    }

    init {
        update()
        LOGGER.info("Loaded ${assetsById.size} assets initially")
    }
}