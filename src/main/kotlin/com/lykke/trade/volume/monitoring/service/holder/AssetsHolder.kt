package com.lykke.trade.volume.monitoring.service.holder

import com.lykke.trade.volume.monitoring.service.entity.Asset

interface AssetsHolder {
    fun getAsset(assetId: String): Asset?
}