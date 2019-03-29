package com.lykke.trade.volume.monitoring.service.isalive

import com.lykke.trade.volume.monitoring.service.entity.IsAliveResponse

interface IsAliveResponseGetter {
    fun getResponse(): IsAliveResponse
}
