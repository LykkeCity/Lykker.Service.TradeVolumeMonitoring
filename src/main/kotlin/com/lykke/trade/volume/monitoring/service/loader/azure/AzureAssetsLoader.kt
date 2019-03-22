package com.lykke.trade.volume.monitoring.service.loader.azure

import com.lykke.trade.volume.monitoring.service.entity.Asset
import com.lykke.trade.volume.monitoring.service.loader.AssetsLoader
import com.lykke.utils.azure.getOrCreateTable
import com.microsoft.azure.storage.table.TableOperation
import com.microsoft.azure.storage.table.TableQuery
import java.util.HashMap

class AzureAssetsLoader(connectionString: String) : AssetsLoader {

    companion object {
        private const val TABLE_NAME = "Dictionaries"
    }

    private val assetsTable = getOrCreateTable(connectionString, TABLE_NAME)

    override fun loadAssetsByIdMap(): Map<String, Asset> {
        val result = HashMap<String, Asset>()
        val partitionQuery = TableQuery.from(AzureAsset::class.java)
                .where(TableQuery.generateFilterCondition("PartitionKey",
                        TableQuery.QueryComparisons.EQUAL,
                        AzureAsset.ASSET_PARTITION_KEY))

        for (asset in assetsTable.execute(partitionQuery)) {
            result[asset.rowKey] = convertAsset(asset)
        }
        return result
    }

    override fun loadAsset(assetId: String): Asset? {
        val operation = TableOperation.retrieve(AzureAsset.ASSET_PARTITION_KEY,
                assetId,
                AzureAsset::class.java)
        val asset = assetsTable.execute(operation).getResultAsType<AzureAsset>()
        return asset?.let { convertAsset(asset) }
    }

    private fun convertAsset(azureAsset: AzureAsset): Asset {
        return Asset(azureAsset.rowKey, azureAsset.accuracy)
    }

}