package com.lykke.trade.volume.monitoring.service

import com.lykke.trade.volume.monitoring.service.utils.equalsIgnoreScale
import java.math.BigDecimal

fun assertEquals(expected: BigDecimal?, actual: BigDecimal?, message: String? = null) {
    if (expected == null && actual == null) {
        return
    }

    if (!equalsIgnoreScale(expected!!, actual!!)) {
        kotlin.test.assertEquals(expected, actual, message)
    }
}