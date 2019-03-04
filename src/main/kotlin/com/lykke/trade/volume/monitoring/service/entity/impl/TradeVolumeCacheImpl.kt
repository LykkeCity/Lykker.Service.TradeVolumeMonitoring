package com.lykke.trade.volume.monitoring.service.entity.impl

import com.lykke.trade.volume.monitoring.service.config.TradeVolumeConfig
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import kotlin.collections.HashMap

@Component
class TradeVolumeCacheImpl(val tradeVolumeConfig: TradeVolumeConfig) : TradeVolumeCache {

    private val tradeVolumesByAssetIdByWalletId = HashMap<String, HashMap<String, MutableList<Volume>>>()
    private val tradeVolumeForPeriodByAssetIdByWalletId = HashMap<String, HashMap<String, BigDecimal>>()
    private val recalculationsByAssetIdByWalletId = HashMap<String, HashMap<String, Long>>()

    override fun add(walletId: String,
                     assetId: String,
                     volume: BigDecimal,
                     timestamp: Date): BigDecimal {
        if (isExpired(timestamp)) {
            return getVolume(walletId = walletId, assetId = assetId)
        }

        removeOldVolumes(walletId, assetId)
        getVolumes(walletId, assetId).add(Volume(timestamp, volume))
        return performVolumeRecalculation(walletId = walletId, assetId = assetId, volumeDelta = volume)
    }

    override fun get(walletId: String, assetId: String): BigDecimal {
        removeOldVolumes(walletId, assetId)
        return getVolume(walletId, assetId)
    }

    override fun clear() {
        tradeVolumesByAssetIdByWalletId.clear()
        tradeVolumeForPeriodByAssetIdByWalletId.clear()
        recalculationsByAssetIdByWalletId.clear()
    }

    private fun removeOldVolumes(walletId: String, assetId: String) {
        val volumes = getVolumes(walletId, assetId)

        if (volumes.size == 0) {
            return
        }

        val volumesIterator = volumes.iterator()

        val oldItemsBound = Date().time - tradeVolumeConfig.tradeVolumeCacheConfig.expiryPeriod
        var removedVolumesSum = BigDecimal.ZERO
        var currentVolume: Volume? = if (volumesIterator.hasNext()) {
            volumesIterator.next()
        } else null

        while (currentVolume != null && currentVolume.timestamp.time <= oldItemsBound) {
            volumesIterator.remove()
            removedVolumesSum = removedVolumesSum.add(currentVolume.volume)
            if (volumesIterator.hasNext()) {
                currentVolume = volumesIterator.next()
            } else {
                currentVolume = null
            }
        }

        performVolumeRecalculation(walletId = walletId,
                assetId = assetId,
                volumeDelta = removedVolumesSum.negate())
    }


    private fun performVolumeRecalculation(walletId: String,
                                           assetId: String,
                                           volumeDelta: BigDecimal): BigDecimal {
        val resultVolume = getVolume(walletId, assetId).add(volumeDelta)
        setVolume(walletId = walletId, assetId = assetId, volume = resultVolume)
        incrementRecalculationsCount(walletId = walletId, assetId =  assetId)
        return resultVolume
    }

    private fun getVolumes(walletId: String, assetId: String): MutableList<Volume> {
        return tradeVolumesByAssetIdByWalletId
                .getOrPut(walletId) { HashMap() }
                .getOrPut(assetId) { LinkedList() }
    }

    private fun getVolume(walletId: String, assetId: String): BigDecimal {
        return tradeVolumeForPeriodByAssetIdByWalletId
                .getOrPut(walletId) { HashMap() }
                .getOrPut(assetId) { BigDecimal.ZERO }
    }

    private fun setVolume(walletId: String, assetId: String, volume: BigDecimal) {
        tradeVolumeForPeriodByAssetIdByWalletId.getOrPut(walletId) { HashMap() }[assetId] = volume
    }

    private fun getRecalculationsCount(walletId: String, assetId: String): Long {
        return recalculationsByAssetIdByWalletId.getOrPut(walletId) { HashMap() }.getOrPut(assetId) { 0L }
    }

    private fun incrementRecalculationsCount(walletId: String, assetId: String) {
        recalculationsByAssetIdByWalletId.getOrPut(walletId) { HashMap() }[assetId] = getRecalculationsCount(walletId, assetId) + 1
    }

    private fun clearRecalucationscount(walletId: String, assetId: String) {
        recalculationsByAssetIdByWalletId.getOrPut(walletId) { HashMap() }.remove(assetId)
    }


    private fun performFullTradeVolumeRecalculation(walletId: String, assetId: String): BigDecimal {
        val result = BigDecimal.ZERO

        getVolumes(walletId, assetId)
                .forEach { result.add(it.volume) }

        return result
    }

    private fun isExpired(date: Date): Boolean {
        return Date().time - tradeVolumeConfig.tradeVolumeCacheConfig.expiryPeriod >= date.time
    }

    private class Volume(val timestamp: Date, val volume: BigDecimal)
}