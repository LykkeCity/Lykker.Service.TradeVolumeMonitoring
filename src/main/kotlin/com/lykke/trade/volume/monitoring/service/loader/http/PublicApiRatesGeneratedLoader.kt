package com.lykke.trade.volume.monitoring.service.loader.http

import com.lykke.trade.volume.monitoring.service.entity.Rate
import com.lykke.trade.volume.monitoring.service.loader.RatesLoader
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.ApiClient
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.api.AssetPairsApi
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model.ApiAssetPairRateModel

class PublicApiRatesGeneratedLoader(publicApiUrl: String) : RatesLoader {

    private val client = AssetPairsApi(ApiClient().setBasePath(publicApiUrl))

    override fun loadRatesByAssetPairIdMap(): Map<String, Rate> {
        val response = client.apiAssetPairsRateGet()
        val ratesByAssetPairId = HashMap<String, Rate>()
        response.iterator().forEach { publicApiRate ->
            convertToRate(publicApiRate)?.let { rate ->
                ratesByAssetPairId[rate.assetPairId] = rate
            }
        }
        return ratesByAssetPairId
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
