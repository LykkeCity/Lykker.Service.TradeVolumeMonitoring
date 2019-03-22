package com.lykke.trade.volume.monitoring.service.loader.azure

import com.microsoft.azure.storage.table.TableServiceEntity

class AzureAssetPair : TableServiceEntity() {

    companion object {
        const val ASSET_PAIR_PARTITION_KEY = "AssetPair"
    }

    lateinit var baseAssetId: String
    lateinit var quotingAssetId: String
    var accuracy: Int = 0
}