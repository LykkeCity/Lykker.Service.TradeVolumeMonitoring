package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.client.accounts.ClientAccountsCache
import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.EventPersistenceData
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.entity.TradeVolumePersistenceData
import com.lykke.trade.volume.monitoring.service.notification.NotificationService
import com.lykke.trade.volume.monitoring.service.persistence.PersistenceManager
import com.lykke.trade.volume.monitoring.service.process.AssetVolumeConverter
import com.lykke.trade.volume.monitoring.service.process.EventProcessLoggerFactory
import com.lykke.trade.volume.monitoring.service.process.TradeVolumesProcessor
import java.math.BigDecimal
import java.util.*

class TradeVolumesProcessorImpl(private val targetAssetId: String,
                                private val converter: AssetVolumeConverter,
                                private val persistenceManager: PersistenceManager,
                                private val tradeVolumeCache: TradeVolumeCache,
                                private val maxVolume: BigDecimal,
                                private val notificationService: NotificationService,
                                private val clientAccountsCache: ClientAccountsCache) : TradeVolumesProcessor {

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
                tradeVolumePersistenceData?.let {
                    tradeVolumesPersistenceData.add(tradeVolumePersistenceData)
                }
            } catch (e: Exception) {
                LOGGER.error(eventTradeVolumesWrapper.eventSequenceNumber,
                        "Unable to process trade volume ($tradeVolume): ${e.message}",
                        e)
            }
        }
        persist(EventPersistenceData(eventTradeVolumesWrapper.eventSequenceNumber,
                eventTradeVolumesWrapper.eventTimestamp,
                tradeVolumesPersistenceData))
    }

    private fun persist(eventPersistenceData: EventPersistenceData) {
        try {
            persistenceManager.persist(eventPersistenceData)
        } catch (e: Exception) {
            LOGGER.error(eventPersistenceData.sequenceNumber, "Unable to persist data: ${e.message}", e)
        }
    }

    private fun processTradeVolume(eventSequenceNumber: Long, tradeVolume: TradeVolume): TradeVolumePersistenceData? {
        val clientId = clientAccountsCache.getClientByWalletId(tradeVolume.walletId)
        if (clientId == null) {
            LOGGER.warn(eventSequenceNumber, "Can not find client by wallet: ${tradeVolume.walletId}")
            return null
        }

        val targetAssetVolume = if (tradeVolume.assetId == targetAssetId)
            tradeVolume.volume
        else
            converter.convert(tradeVolume.assetId, tradeVolume.volume, targetAssetId)

        val volumesForThePeriod = tradeVolumeCache.add(eventSequenceNumber,
                tradeVolume.tradeIdx,
                clientId,
                tradeVolume.assetId,
                targetAssetVolume,
                tradeVolume.timestamp)
        LOGGER.info(eventSequenceNumber, "Processed trade volume ($tradeVolume), clientId: $clientId, targetAsset: $targetAssetId, " +
                "targetAssetVolume: $targetAssetVolume")

        sendMailNotificationsIfNeeded(eventSequenceNumber, clientId, tradeVolume.assetId, volumesForThePeriod)

        return TradeVolumePersistenceData(eventSequenceNumber,
                tradeVolume.tradeIdx,
                clientId,
                tradeVolume.assetId,
                targetAssetVolume,
                tradeVolume.timestamp)
    }

    private fun sendMailNotificationsIfNeeded(eventSequenceNumber: Long, clientId: String, assetId: String, volumes: List<Pair<Long, BigDecimal>>) {
        volumes.forEach { volume ->
            if (volume.second >= maxVolume) {
                LOGGER.info(eventSequenceNumber, "Trade volume limit reached for client $clientId, assetId: $assetId")
                notificationService.sendTradeVolumeLimitReachedMailNotification(clientId, assetId, Date(volume.first))
                return
            }
        }
    }
}