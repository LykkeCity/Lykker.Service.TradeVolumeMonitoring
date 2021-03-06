package com.lykke.trade.volume.monitoring.service.loader.http

import com.lykke.trade.volume.monitoring.service.entity.Rate
import com.lykke.trade.volume.monitoring.service.loader.RatesLoader
import com.lykke.trade.volume.monitoring.service.http.generated.client.ApiClient
import com.lykke.trade.volume.monitoring.service.http.generated.client.api.AssetPairsApi
import com.lykke.trade.volume.monitoring.service.http.generated.client.model.ApiAssetPairRateModel

class PublicApiRatesLoader(publicApiUrl: String) : RatesLoader {

    private val client = AssetPairsApi(ApiClient().setBasePath(publicApiUrl))

    override fun loadRatesByAssetPairIdMap(): Map<String, Rate> {
        return client.apiAssetPairsRateGet()
                .asSequence()
                .mapNotNull(::convertToRate)
                .groupBy { it.assetPairId }
                .mapValues { it.value.single() }
    }

    override fun loadRate(assetPairId: String): Rate? {
        return convertToRate(client.apiAssetPairsRateByAssetPairIdGet(assetPairId))
    }

    private fun convertToRate(publicApiRate: ApiAssetPairRateModel): Rate? {
        return publicApiRate.id?.let {
            Rate(it,
                    publicApiRate.bid?.toBigDecimal(),
                    publicApiRate.ask?.toBigDecimal())
        }
    }
}
