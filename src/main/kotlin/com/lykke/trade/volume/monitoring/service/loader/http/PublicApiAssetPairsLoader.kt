package com.lykke.trade.volume.monitoring.service.loader.http

import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import com.lykke.trade.volume.monitoring.service.http.generated.client.ApiClient
import com.lykke.trade.volume.monitoring.service.http.generated.client.api.AssetPairsApi
import com.lykke.trade.volume.monitoring.service.http.generated.client.model.ApiAssetPair

class PublicApiAssetPairsLoader(publicApiUrl: String) : AssetPairsLoader {

    companion object {
        private const val MARKET_PARAM_VALUE = "Spot"
    }

    private val client = AssetPairsApi(ApiClient().setBasePath(publicApiUrl))

    override fun loadAssetPairsByIdMap(): Map<String, AssetPair> {
        return client.apiAssetPairsDictionaryByMarketGet(MARKET_PARAM_VALUE)
                .asSequence()
                .mapNotNull(::convertToAssetPair)
                .groupBy { it.id }
                .mapValues { it.value.single() }
    }

    private fun convertToAssetPair(publicApiAssetPair: ApiAssetPair): AssetPair? {
        return publicApiAssetPair.id?.let {
            AssetPair(publicApiAssetPair.id,
                    publicApiAssetPair.baseAssetId,
                    publicApiAssetPair.quotingAssetId,
                    publicApiAssetPair.accuracy)
        }
    }
}