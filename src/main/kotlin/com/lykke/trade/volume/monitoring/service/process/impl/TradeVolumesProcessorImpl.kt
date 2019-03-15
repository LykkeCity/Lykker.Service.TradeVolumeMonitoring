package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.process.AssetVolumeConverter
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor

class TradeVolumesProcessorImpl(private val targetAssetId: String,
                                private val converter: AssetVolumeConverter,
                                private val tradeVolumeCache: TradeVolumeCache) : TradeVolumesProcessor {

    companion object {
        private val LOGGER = EventProcessLoggerFactory.getLogger(TradeVolumesProcessorImpl::class.java.name)
    }

    override fun process(eventTradeVolumesWrapper: EventTradeVolumesWrapper) {
        eventTradeVolumesWrapper.tradeVolumes.forEach { tradeVolume ->
            try {
                processTradeVolume(eventTradeVolumesWrapper.messageId, tradeVolume)
            } catch (e: Exception) {
                LOGGER.error(eventTradeVolumesWrapper.messageId,
                        "Unable to process trade volume ($tradeVolume): ${e.message}",
                        e)
            }
        }
    }

    private fun processTradeVolume(messageId: String, tradeVolume: TradeVolume) {
        val targetAssetVolume = if (tradeVolume.assetId == targetAssetId)
            tradeVolume.volume
        else
            converter.convert(tradeVolume.assetId, tradeVolume.volume, targetAssetId)

        tradeVolumeCache.add(tradeVolume.walletId,
                tradeVolume.assetId,
                targetAssetVolume,
                tradeVolume.timestamp)

        LOGGER.info(messageId, "Processed trade volume ($tradeVolume), " +
                "targetAsset: $targetAssetId, " +
                "targetAssetVolume: $targetAssetVolume")
    }
}