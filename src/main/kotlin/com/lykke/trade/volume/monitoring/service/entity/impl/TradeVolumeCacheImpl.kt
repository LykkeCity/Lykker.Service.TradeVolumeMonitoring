package com.lykke.trade.volume.monitoring.service.entity.impl

import com.lykke.trade.volume.monitoring.service.config.Config
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

@Component
class TradeVolumeCacheImpl(val config: Config) : TradeVolumeCache {

    private val tradeVolumesByClientIdByAssetId = ConcurrentHashMap<String, NavigableSet<Volume>>()
    private val lockByClientIdAssetId = ConcurrentHashMap<String, Any>()
    private val cumulativeVolumeByTradeVolume = WeakHashMap<String, BigDecimal>()
    private val cacheConfig = config.tradeVolumeConfig.tradeVolumeCacheConfig

    override fun add(clientId: String,
                     assetId: String,
                     volume: BigDecimal,
                     timestamp: Date): Map<Long, BigDecimal> {
        val clientIdAssetId = getClientVolumesKey(clientId, assetId)
        synchronized(getLock(clientIdAssetId)) {
            val volumes = getVolumes(clientIdAssetId)
            val volumeToAdd = Volume(timestamp, volume)
            volumes.add(volumeToAdd)
            cumulativeVolumeByTradeVolume[getVolumeKey(volumeToAdd, clientId, assetId)] = getCumulativeVolumeForTradeVolume(clientId, assetId, volumeToAdd, volumes)
            return getExceededLimitVolumes(
                    clientId,
                    assetId,
                    volumeToAdd,
                    volumes)
        }
    }

    @Scheduled(fixedRateString = "#{Config.}")
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
            volumes.pollLast()
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

    private fun getExceededLimitVolumes(clientId: String,
                                        assetId: String,
                                        volume: Volume,
                                        volumes: NavigableSet<Volume>): Map<Long, BigDecimal> {
        val result = HashMap<Long, BigDecimal>()
        val volumesIterator = volumes.iterator()

        while (volumesIterator.hasNext()) {
            val currentVolume = volumesIterator.next()
            if (currentVolume != volume) {
                cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume, clientId, assetId)] = cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume, clientId, assetId)]!!.add(volume.volume)
            }

            if (currentVolume.notified) {
                continue
            }

            val periodBoundVolume = volumes.floor(Volume(Date(currentVolume.timestamp.time - cacheConfig.volumePeriod), BigDecimal.ZERO))
            val volumeForPeriod = if (periodBoundVolume != null) {
                cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume, clientId, assetId)]!! - (cumulativeVolumeByTradeVolume[getVolumeKey(periodBoundVolume, clientId, assetId)]!! - periodBoundVolume.volume)
            } else {
                currentVolume.volume
            }

            if (volumeForPeriod >= config.tradeVolumeConfig.maxVolume) {
                currentVolume.notified = true
                result[currentVolume.timestamp.time] = volumeForPeriod
            }

            if (currentVolume == volume) {
                break
            }
        }

        return result
    }

    private fun getLock(clientIdAssetId: String): Any {
        lockByClientIdAssetId.putIfAbsent(clientIdAssetId, Object())
        return lockByClientIdAssetId[clientIdAssetId] as Any
    }

    private fun getVolumeKey(volume: Volume, clientId: String, assetId: String): String {
        return "${volume.timestamp.time}_${clientId}_$assetId"
    }

    private fun getClientVolumesKey(clientId: String, assetId: String): String {
        return "${clientId}_$assetId"
    }

    private fun getVolumes(clientIdAssetId: String): NavigableSet<Volume> {
        return tradeVolumesByClientIdByAssetId
                .getOrPut(clientIdAssetId) { TreeSet() }
    }

    private class Volume(val timestamp: Date,
                         val volume: BigDecimal,
                         var notified: Boolean = false) : Comparable<Volume> {
        override fun compareTo(other: Volume): Int {
            return other.timestamp.compareTo(this.timestamp)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Volume

            if (timestamp != other.timestamp) return false
            if (volume != other.volume) return false

            return true
        }

        override fun hashCode(): Int {
            var result = timestamp.hashCode()
            result = 31 * result + volume.hashCode()
            return result
        }
    }
}