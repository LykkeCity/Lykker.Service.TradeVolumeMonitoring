package com.lykke.trade.volume.monitoring.service.loader.azure

import com.microsoft.azure.storage.table.TableServiceEntity

class AzureAsset : TableServiceEntity() {

    companion object {
        const val ASSET_PARTITION_KEY = "Asset"
    }

    var accuracy: Int = 0
}