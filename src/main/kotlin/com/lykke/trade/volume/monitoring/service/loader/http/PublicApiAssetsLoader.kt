package com.lykke.trade.volume.monitoring.service.loader.http

import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.loader.AssetsLoader
import com.lykke.trade.volume.monitoring.service.http.generated.client.ApiClient
import com.lykke.trade.volume.monitoring.service.http.generated.client.api.AssetsApi
import com.lykke.trade.volume.monitoring.service.http.generated.client.model.ApiAsset

class PublicApiAssetsLoader(publicApiUrl: String) : AssetsLoader {

    private val client = AssetsApi(ApiClient().setBasePath(publicApiUrl))

    override fun loadAssetsByIdMap(): Map<String, Asset> {
        return client.apiAssetsDictionaryGet()
                .asSequence()
                .mapNotNull(::convertToAsset)
                .groupBy { it.id }
                .mapValues { it.value.single() }
    }

    override fun loadAsset(assetId: String): Asset? {
        // Not implemented in public api
        return null
    }

    private fun convertToAsset(publicApiAsset: ApiAsset): Asset {
        return Asset(publicApiAsset.id, publicApiAsset.accuracy)
    }

}