package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.PersistenceData
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumePersistenceData
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
import com.lykke.trade.volume.monitoring.service.process.AssetVolumeConverter
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor

class TradeVolumesProcessorImpl(private val targetAssetId: String,
                                private val converter: AssetVolumeConverter,
                                private val persistenceManager: PersistenceManager,
                                private val tradeVolumeCache: TradeVolumeCache) : TradeVolumesProcessor {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(TradeVolumesProcessorImpl::class.java.name)
    }

    override fun process(eventTradeVolumesWrapper: EventTradeVolumesWrapper) {
        if (eventTradeVolumesWrapper.tradeVolumes.isEmpty()) {
            return
        }
        val tradeVolumesPersistenceData = ArrayList<TradeVolumePersistenceData>(eventTradeVolumesWrapper.tradeVolumes.size)
        eventTradeVolumesWrapper.tradeVolumes.forEach { tradeVolume ->
            try {
                val tradeVolumePersistenceData = processTradeVolume(eventTradeVolumesWrapper.eventSequenceNumber, tradeVolume)
                tradeVolumesPersistenceData.add(tradeVolumePersistenceData)
            } catch (e: Exception) {
                LOGGER.error(eventTradeVolumesWrapper.eventSequenceNumber,
                        "Unable to process trade volume ($tradeVolume): ${e.message}",
                        e)
            }
        }
        persistenceManager.persist(PersistenceData(eventTradeVolumesWrapper.eventSequenceNumber, tradeVolumesPersistenceData))
    }

    private fun processTradeVolume(eventSequenceNumber: Long, tradeVolume: TradeVolume): TradeVolumePersistenceData {

        val targetAssetVolume = if (tradeVolume.assetId == targetAssetId)
            tradeVolume.volume
        else
            converter.convert(tradeVolume.assetId, tradeVolume.volume, targetAssetId)

        val clientId = tradeVolume.walletId // todo: additional task: map walletId -> clientId using lib

        tradeVolumeCache.add(eventSequenceNumber,
                tradeVolume.tradeIdx,
                clientId,
                tradeVolume.assetId,
                targetAssetVolume,
                tradeVolume.timestamp)

        // todo: additional task: check limit and send notification

        LOGGER.info(eventSequenceNumber, "Processed trade volume ($tradeVolume), clientId: $clientId, targetAsset: $targetAssetId, " +
                "targetAssetVolume: $targetAssetVolume")

        return TradeVolumePersistenceData(eventSequenceNumber,
                tradeVolume.tradeIdx,
                clientId,
                tradeVolume.assetId,
                targetAssetVolume,
                tradeVolume.timestamp)
    }
}