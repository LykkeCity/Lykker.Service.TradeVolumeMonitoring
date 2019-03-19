package com.lykke.trade.volume.monitoring.service.entity.impl

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class TradeVolumeCacheImpl(config: Config) : TradeVolumeCache {

    private val cacheConfig = config.tradeVolumeConfig.tradeVolumeCacheConfig

    private val tradeVolumesByClientIdByAssetId = ConcurrentHashMap<String, NavigableSet<Volume>>()
    private val lockByClientIdAssetId = ConcurrentHashMap<String, Any>()
    private val cumulativeVolumeByTradeVolume = ConcurrentHashMap<String, BigDecimal>()

    override fun add(clientId: String,
                     assetId: String,
                     tradeIdx: Int,
                     volume: BigDecimal,
                     timestamp: Date): List<Pair<Long, BigDecimal>> {
        val clientIdAssetId = getClientVolumesKey(clientId, assetId)
        synchronized(getLock(clientIdAssetId)) {
            val volumes = getVolumes(clientIdAssetId)
            val volumeToAdd = Volume(tradeIdx, timestamp, volume, clientId, assetId)
            volumes.add(volumeToAdd)
            cumulativeVolumeByTradeVolume[getVolumeKey(volumeToAdd, clientId, assetId)] = getCumulativeVolumeForTradeVolume(clientId, assetId, volumeToAdd, volumes)
            return getTradeVolumesForPeriod(
                    clientId,
                    assetId,
                    volumeToAdd,
                    volumes)
        }
    }

    @Scheduled(fixedRateString = "#{Config.tradeVolumeConfig.tradeVolumeCacheConfig.cleanCacheInterval}")
    private fun cleanCache() {
        tradeVolumesByClientIdByAssetId.forEach { clientIdAssetId, volumes ->
            synchronized(getLock(clientIdAssetId)) {
                removeOldVolumes(volumes)
                if (volumes.isEmpty()) {
                    tradeVolumesByClientIdByAssetId.remove(clientIdAssetId)
                    lockByClientIdAssetId.remove(clientIdAssetId)
                }
            }
        }
    }

    private fun removeOldVolumes(volumes: NavigableSet<Volume>) {
        while (!volumes.isEmpty() && isExpired(volumes.last())) {
            val removedVolume = volumes.pollLast()
            cumulativeVolumeByTradeVolume.remove(getVolumeKey(removedVolume, removedVolume.clientId, removedVolume.assetId))
        }
    }

    private fun isExpired(volume: Volume) =
            volume.timestamp.time <= Date().time - cacheConfig.expiryRatio * cacheConfig.volumePeriod

    private fun getCumulativeVolumeForTradeVolume(
            clientId: String,
            assetId: String,
            volume: Volume,
            volumes: NavigableSet<Volume>): BigDecimal {
        val higherVolume = volumes.higher(volume)
        return if (higherVolume != null) {
            cumulativeVolumeByTradeVolume[getVolumeKey(higherVolume, clientId, assetId)]!!.add(volume.volume)
        } else {
            volume.volume
        }
    }

    private fun getTradeVolumesForPeriod(clientId: String,
                                         assetId: String,
                                         volume: Volume,
                                         volumes: NavigableSet<Volume>): List<Pair<Long, BigDecimal>> {
        val result = ArrayList<Pair<Long, BigDecimal>>()
        val volumesIterator = volumes.iterator()

        updateCumulativeVolumes(clientId, assetId, volume, volumes)

        while (volumesIterator.hasNext()) {
            val currentVolume = volumesIterator.next()

            val periodBoundVolume = volumes.floor(Volume(Integer.MAX_VALUE, Date(currentVolume.timestamp.time - cacheConfig.volumePeriod), BigDecimal.ZERO, clientId, assetId))
            val volumeForPeriod = if (periodBoundVolume != null) {
                cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume, clientId, assetId)]!! - (cumulativeVolumeByTradeVolume[getVolumeKey(periodBoundVolume, clientId, assetId)]!! - periodBoundVolume.volume)
            } else {
                currentVolume.volume
            }

            result.add(currentVolume.timestamp.time to volumeForPeriod)

            if (currentVolume == volume) {
                break
            }
        }

        return result
    }

    private fun updateCumulativeVolumes(clientId: String,
                                        assetId: String,
                                        volume: Volume,
                                        volumes: NavigableSet<Volume>) {
        val volumesIterator = volumes.iterator()

        while (volumesIterator.hasNext()) {
            val currentVolume = volumesIterator.next()
            if (currentVolume == volume) {
                return
            }
            cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume, clientId, assetId)] = cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume, clientId, assetId)]!!.add(volume.volume)
        }
    }

    private fun getLock(clientIdAssetId: String): Any {
        lockByClientIdAssetId.putIfAbsent(clientIdAssetId, Object())
        return lockByClientIdAssetId[clientIdAssetId] as Any
    }

    private fun getVolumeKey(volume: Volume, clientId: String, assetId: String): String {
        return "${volume.tradeIdx}_${volume.timestamp.time}_${clientId}_$assetId"
    }

    private fun getClientVolumesKey(clientId: String, assetId: String): String {
        return "${clientId}_$assetId"
    }

    private fun getVolumes(clientIdAssetId: String): NavigableSet<Volume> {
        return tradeVolumesByClientIdByAssetId
                .getOrPut(clientIdAssetId) { TreeSet() }
    }

    private class Volume(val tradeIdx: Int,
                         val timestamp: Date,
                         val volume: BigDecimal,
                         val clientId: String,
                         val assetId: String) : Comparable<Volume> {
        override fun compareTo(other: Volume): Int {
             return Comparator
                     .comparingLong<Volume> { volume -> volume.timestamp.time }.reversed()
                     .thenComparing(Comparator.comparingInt { volume ->  volume.tradeIdx}).reversed()
                     .compare(other, this)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Volume

            if (tradeIdx != other.tradeIdx) return false
            if (timestamp != other.timestamp) return false
            if (volume != other.volume) return false
            if (clientId != other.clientId) return false
            if (assetId != other.assetId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tradeIdx
            result = 31 * result + timestamp.hashCode()
            result = 31 * result + volume.hashCode()
            result = 31 * result + clientId.hashCode()
            result = 31 * result + assetId.hashCode()
            return result
        }


    }
}