package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.exception.ApplicationException
import com.lykke.trade.volume.monitoring.service.holder.AssetPairsHolder
import com.lykke.trade.volume.monitoring.service.holder.AssetsHolder
import com.lykke.trade.volume.monitoring.service.holder.PricesHolder
import com.lykke.trade.volume.monitoring.service.process.AssetVolumeConverter
import com.lykke.trade.volume.monitoring.service.utils.divideWithMaxScale
import java.math.BigDecimal

class AssetVolumeConverterImpl(private val assetsHolder: AssetsHolder,
                               private val assetPairsHolder: AssetPairsHolder,
                               private val pricesHolder: PricesHolder) : AssetVolumeConverter {

    override fun convert(assetId: String, volume: BigDecimal, targetAssetId: String): BigDecimal {
        val targetAsset = assetsHolder.getAsset(targetAssetId)
                ?: throw ApplicationException("Unknown asset $targetAssetId")

        val assetPair = assetPairsHolder.getAssetPair(assetId, targetAssetId)
                ?: throw ApplicationException("Unable to find asset pair for assets: $assetId, $targetAssetId")

        val price = pricesHolder.getPrice(assetPair.id)
                ?: throw ApplicationException("Unable to define price for asset pair ${assetPair.id}")

        return (if (assetPair.baseAssetId == targetAssetId)
            divideWithMaxScale(volume, price)
        else
            volume * price)
                .setScale(targetAsset.accuracy, BigDecimal.ROUND_HALF_UP)
    }

}