package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper

interface TradeVolumesProcessor {
    fun process(eventTradeVolumesWrapper: EventTradeVolumesWrapper)
}