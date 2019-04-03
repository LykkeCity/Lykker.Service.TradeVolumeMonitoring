package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.AssetPair
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

    override fun convert(assetId: String,
                         volume: BigDecimal,
                         crossAssetIds: List<String>,
                         targetAssetId: String): BigDecimal {
        if (assetId == targetAssetId) {
            return volume
        }
        val targetAsset = assetsHolder.getAsset(targetAssetId)
                ?: throw ApplicationException("Unknown asset $targetAssetId")
        val conversionCoef = calculateConversionCoef(assetId, crossAssetIds, targetAsset.id)
        return (volume * conversionCoef).setScale(targetAsset.accuracy, BigDecimal.ROUND_HALF_UP)
    }

    private fun calculateConversionCoef(assetId: String,
                                        crossAssetIds: List<String>,
                                        targetAssetId: String): BigDecimal {
        return try {
            calculateStraightConversionCoef(assetId, targetAssetId)
        } catch (e: ApplicationException) {
            if (crossAssetIds.isEmpty()) {
                throw e
            }
            calculateCrossAssetsConversionCoef(assetId, crossAssetIds, targetAssetId)
        }
    }

    private fun calculateStraightConversionCoef(assetId: String,
                                                targetAssetId: String): BigDecimal {
        val assetPair = getAssetPair(assetId, targetAssetId)
        return calculateOppositeAssetConversionCoef(assetId, assetPair)
    }

    private fun getAssetPair(assetId1: String, assetId2: String): AssetPair {
        return assetPairsHolder.getAssetPair(assetId1, assetId2)
                ?: throw ApplicationException("Unable to find asset pair for assets: $assetId1, $assetId2")
    }

    private fun calculateCrossAssetsConversionCoef(assetId: String,
                                                   crossAssetIds: List<String>,
                                                   targetAssetId: String): BigDecimal {
        var errorMessage: StringBuilder? = null
        crossAssetIds.forEach {
            try {
                return calculateCrossAssetConversionCoef(assetId, it, targetAssetId)
            } catch (e: ApplicationException) {
                if (errorMessage == null) {
                    errorMessage = StringBuilder()
                } else {
                    errorMessage!!.append(", ")
                }
                errorMessage!!.append(e.message)
            }
        }
        throw ApplicationException("Unable to calculate cross conversion coefficient: $errorMessage")
    }

    private fun calculateCrossAssetConversionCoef(assetId: String,
                                                  crossAssetId: String,
                                                  targetAssetId: String): BigDecimal {
        return calculateStraightConversionCoef(assetId, crossAssetId) * calculateStraightConversionCoef(crossAssetId, targetAssetId)
    }

    private fun calculateOppositeAssetConversionCoef(sourceAssetId: String, assetPair: AssetPair): BigDecimal {
        val price = pricesHolder.getPrice(assetPair.id)
                ?: throw ApplicationException("Unable to define price for asset pair ${assetPair.id}")

        return if (assetPair.baseAssetId == sourceAssetId)
            price
        else
            divideWithMaxScale(BigDecimal.ONE, price)
    }
}