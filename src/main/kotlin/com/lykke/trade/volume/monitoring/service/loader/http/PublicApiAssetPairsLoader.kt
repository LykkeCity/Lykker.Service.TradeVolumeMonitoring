package com.lykke.trade.volume.monitoring.service.loader.http

import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import io.swagger.client.ApiClient
import io.swagger.client.api.AssetPairsApi
import io.swagger.client.model.ApiAssetPair

class PublicApiAssetPairsLoader(publicApiUrl: String) : AssetPairsLoader {

    private val client = AssetPairsApi(ApiClient().setBasePath(publicApiUrl))

    override fun loadAssetPairsByIdMap(): Map<String, AssetPair> {
        return client.apiAssetPairsDictionaryByMarketGet("Spot")
                .asSequence()
                .mapNotNull(::convertToAssetPair)
                .groupBy { it.id }
                .mapValues { it.value.single() }
    }

    private fun convertToAssetPair(publicApiAssetPair: ApiAssetPair): AssetPair {
        return AssetPair(publicApiAssetPair.id,
                publicApiAssetPair.baseAssetId,
                publicApiAssetPair.quotingAssetId,
                publicApiAssetPair.accuracy)
    }
}