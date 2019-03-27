package com.lykke.trade.volume.monitoring.service.entity.impl

import com.lykke.trade.volume.monitoring.service.config.TradeVolumeCacheConfig
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.ArrayList
import java.util.Comparator
import java.util.Date
import java.util.NavigableSet
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
class TradeVolumeCacheImpl(@Value("#{Config.tradeVolumeConfig.tradeVolumeCacheConfig}")
                           private val cacheConfig: TradeVolumeCacheConfig,
                           private val loader: EventsLoader) : TradeVolumeCache {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TradeVolumeCacheImpl::class.java.name)
    }

    private val tradeVolumesByClientIdByAssetId = ConcurrentHashMap<String, NavigableSet<Volume>>()
    private val lockByClientIdAssetId = ConcurrentHashMap<String, Any>()
    private val cumulativeVolumeByTradeVolume = ConcurrentHashMap<String, BigDecimal>()

    @PostConstruct
    fun init() {
        val allPersistenceData = loader.loadEvents().sortedBy { it.timestamp }
        allPersistenceData.forEach { persistenceData ->
            persistenceData.tradeVolumes.forEach { tradeVolumesPersistenceData ->
                add(persistenceData.sequenceNumber,
                        tradeVolumesPersistenceData.tradeIdx,
                        tradeVolumesPersistenceData.clientId,
                        tradeVolumesPersistenceData.assetId,
                        tradeVolumesPersistenceData.targetAssetVolume,
                        tradeVolumesPersistenceData.timestamp)
            }
        }
        LOGGER.info("Initialized with events: ${allPersistenceData.size}" +
                ", tradeVolumes: ${allPersistenceData.flatMap { it.tradeVolumes }.size}")

        cleanCache()

        LOGGER.info("Trade volumes after initial cleaning cache: " +
                "${tradeVolumesByClientIdByAssetId.flatMap { it.value }.size}")
    }

    override fun add(eventSequenceNumber: Long,
                     tradeIdx: Int,
                     clientId: String,
                     assetId: String,
                     volume: BigDecimal,
                     timestamp: Date): List<Pair<Long, BigDecimal>> {
        val clientIdAssetId = getClientVolumesKey(clientId, assetId)
        synchronized(getLock(clientIdAssetId)) {
            val volumes = getVolumes(clientIdAssetId)
            val volumeToAdd = Volume(eventSequenceNumber, tradeIdx, timestamp, volume, clientId, assetId)
            volumes.add(volumeToAdd)
            cumulativeVolumeByTradeVolume[getVolumeKey(volumeToAdd)] = getCumulativeVolumeForTradeVolume(volumeToAdd, volumes)

            return getTradeVolumesForPeriod(volumeToAdd, volumes)
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
        LOGGER.debug("Trade volumes after cleaning cache: ${tradeVolumesByClientIdByAssetId.flatMap { it.value }.size}")
    }

    private fun removeOldVolumes(volumes: NavigableSet<Volume>) {
        while (!volumes.isEmpty() && isExpired(volumes.last())) {
            val removedVolume = volumes.pollLast()
            cumulativeVolumeByTradeVolume.remove(getVolumeKey(removedVolume))
        }
    }

    private fun isExpired(volume: Volume) =
            volume.timestamp.time <= Date().time - cacheConfig.expiryRatio * cacheConfig.volumePeriod

    private fun getCumulativeVolumeForTradeVolume(volume: Volume, volumes: NavigableSet<Volume>): BigDecimal {
        val cumulativeVolume = getCumulativeVolume(volume, volumes)

        return if (cumulativeVolume != null) {
            cumulativeVolume.add(volume.volume)
        } else {
            volume.volume
        }
    }

    private fun getCumulativeVolume(volume: Volume, volumes: NavigableSet<Volume>): BigDecimal? {
        return volumes.higher(volume)?.let {
            cumulativeVolumeByTradeVolume[getVolumeKey(it)]!!
        } ?: volumes.lower(volume)?.let {
            cumulativeVolumeByTradeVolume[getVolumeKey(it)]!!.subtract(it.volume)
        }
    }

    private fun getTradeVolumesForPeriod(volume: Volume, volumes: NavigableSet<Volume>): List<Pair<Long, BigDecimal>> {
        val result = ArrayList<Pair<Long, BigDecimal>>()
        val volumesIterator = volumes.iterator()

        updateCumulativeVolumes(volume, volumes)

        while (volumesIterator.hasNext()) {
            val currentVolume = volumesIterator.next()

            val periodBoundVolume = volumes.floor(Volume(Long.MAX_VALUE, Integer.MAX_VALUE, Date(currentVolume.timestamp.time - cacheConfig.volumePeriod), BigDecimal.ZERO, volume.clientId, volume.assetId))
            val volumeForPeriod = if (periodBoundVolume != null) {
                cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume)]!! - (cumulativeVolumeByTradeVolume[getVolumeKey(periodBoundVolume)]!! - periodBoundVolume.volume)
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

    private fun updateCumulativeVolumes(volume: Volume, volumes: NavigableSet<Volume>) {
        val volumesIterator = volumes.iterator()

        while (volumesIterator.hasNext()) {
            val currentVolume = volumesIterator.next()
            if (currentVolume == volume) {
                return
            }
            cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume)] = cumulativeVolumeByTradeVolume[getVolumeKey(currentVolume)]!!.add(volume.volume)
        }
    }

    private fun getLock(clientIdAssetId: String): Any {
        lockByClientIdAssetId.putIfAbsent(clientIdAssetId, Object())
        return lockByClientIdAssetId[clientIdAssetId] as Any
    }

    private fun getVolumeKey(volume: Volume): String {
        return "${volume.eventSequenceNumber}_${volume.tradeIdx}_${volume.timestamp.time}_${volume.clientId}_${volume.assetId}"
    }

    private fun getClientVolumesKey(clientId: String, assetId: String): String {
        return "${clientId}_$assetId"
    }

    private fun getVolumes(clientIdAssetId: String): NavigableSet<Volume> {
        return tradeVolumesByClientIdByAssetId
                .getOrPut(clientIdAssetId) { TreeSet() }
    }

    private data class Volume(val eventSequenceNumber: Long,
                              val tradeIdx: Int,
                              val timestamp: Date,
                              val volume: BigDecimal,
                              val clientId: String,
                              val assetId: String) : Comparable<Volume> {
        override fun compareTo(other: Volume): Int {
            return Comparator
                    .comparingLong<Volume> { volume -> volume.timestamp.time }
                    .thenComparing(Comparator.comparingLong { volume -> volume.eventSequenceNumber })
                    .thenComparing(Comparator.comparingInt { volume -> volume.tradeIdx })
                    .reversed()
                    .compare(this, other)
        }
    }
}