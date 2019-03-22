package com.lykke.trade.volume.monitoring.service.loader.azure

import com.lykke.trade.volume.monitoring.service.entity.AssetPair
import com.lykke.trade.volume.monitoring.service.loader.AssetPairsLoader
import com.lykke.utils.azure.getOrCreateTable
import com.microsoft.azure.storage.table.TableQuery
import java.util.HashMap

class AzureAssetPairsLoader(connectionString: String) : AssetPairsLoader {

    companion object {
        private const val TABLE_NAME = "Dictionaries"
    }

    private val assetPairsTable = getOrCreateTable(connectionString, TABLE_NAME)

    override fun loadAssetPairsByIdMap(): Map<String, AssetPair> {
        val result = HashMap<String, AssetPair>()
        val partitionQuery = TableQuery.from(AzureAssetPair::class.java)
                .where(TableQuery.generateFilterCondition("PartitionKey",
                        TableQuery.QueryComparisons.EQUAL,
                        AzureAssetPair.ASSET_PAIR_PARTITION_KEY))

        for (assetPair in assetPairsTable.execute(partitionQuery)) {
            result[assetPair.rowKey] = convertAssetPair(assetPair)
        }
        return result
    }

    private fun convertAssetPair(azureAssetPair: AzureAssetPair): AssetPair {
        return AssetPair(azureAssetPair.rowKey,
                azureAssetPair.baseAssetId,
                azureAssetPair.quotingAssetId,
                azureAssetPair.accuracy)
    }

}