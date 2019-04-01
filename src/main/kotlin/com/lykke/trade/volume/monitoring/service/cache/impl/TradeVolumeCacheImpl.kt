package com.lykke.trade.volume.monitoring.service.cache.impl

import com.lykke.trade.volume.monitoring.service.config.TradeVolumeCacheConfig
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.entity.ClientTradeVolume
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

    private val tradeVolumesByClientIdByAssetId = ConcurrentHashMap<String, ConcurrentHashMap<String, NavigableSet<Volume>>>()
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
                "${tradeVolumesByClientIdByAssetId
                        .map { it.value }
                        .flatMap { it.values }
                        .size}")
    }

    override fun add(eventSequenceNumber: Long,
                     tradeIdx: Int,
                     clientId: String,
                     assetId: String,
                     volume: BigDecimal,
                     timestamp: Date): List<Pair<Long, BigDecimal>> {
        val lockKey = getLockKey(clientId, assetId)
        synchronized(getLock(lockKey)) {
            val volumes = getVolumes(clientId, assetId)
            val volumeToAdd = Volume(eventSequenceNumber, tradeIdx, timestamp, volume, clientId, assetId)
            volumes.add(volumeToAdd)
            cumulativeVolumeByTradeVolume[getVolumeKey(volumeToAdd)] = getCumulativeVolumeForTradeVolume(volumeToAdd, volumes)
            updateCumulativeVolumes(volumeToAdd, volumes)

            return getTradeVolumesForPeriod(volumeToAdd, volumes)
        }
    }

    override fun getTradeVolumeForLastPeriod(clientId: String, assetId: String): ClientTradeVolume? {
        val tradeVolumes = tradeVolumesByClientIdByAssetId[clientId]?.get(assetId)
        val tradeVolume = tradeVolumes?.first() ?: return null

        val tradeVolumesForPeriod = getTradeVolumesForPeriod(tradeVolume, tradeVolumes)
        if (tradeVolumesForPeriod.isEmpty()) {
            return null
        }

        return ClientTradeVolume(clientId, assetId, tradeVolumesForPeriod.single().second, Date(tradeVolumesForPeriod.single().first))
    }

    override fun getTradeVolumesForLastPeriod(clientId: String): List<ClientTradeVolume> {
        val result = ArrayList<ClientTradeVolume>()
        tradeVolumesByClientIdByAssetId[clientId]?.forEach { assetId, volumes ->
            val tradeVolumesForPeriod = getTradeVolumesForPeriod(volumes.first(), volumes)
            if (!tradeVolumesForPeriod.isEmpty()) {
                result.add(ClientTradeVolume(clientId, assetId, tradeVolumesForPeriod.single().second, Date(tradeVolumesForPeriod.single().first)))
            }
        }

        return result
    }

    @Scheduled(fixedRateString = "#{Config.tradeVolumeConfig.tradeVolumeCacheConfig.cleanCacheInterval}")
    private fun cleanCache() {
        tradeVolumesByClientIdByAssetId.forEach { clientId, volumesByAssetId ->
            volumesByAssetId.forEach { assetId, volumes ->
                val lockKey = getLockKey(clientId, assetId)
                synchronized(getLock(lockKey)) {
                    removeOldVolumes(volumes)
                    if (volumes.isEmpty()) {
                        volumesByAssetId.remove(assetId)
                        lockByClientIdAssetId.remove(lockKey)
                    }
                }
            }

            if (volumesByAssetId.isEmpty()) {
                tradeVolumesByClientIdByAssetId.remove(clientId)
            }
        }
        LOGGER.debug("Trade volumes after cleaning cache: ${tradeVolumesByClientIdByAssetId
                .map { it.value }
                .flatMap { it.values }.size}")
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

    private fun getLockKey(clientId: String, assetId: String): String {
        return "${clientId}_$assetId"
    }

    private fun getVolumes(clientId: String, assetId: String): NavigableSet<Volume> {
        return tradeVolumesByClientIdByAssetId
                .getOrPut(clientId) { ConcurrentHashMap() }
                .getOrPut(assetId) { TreeSet() }
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