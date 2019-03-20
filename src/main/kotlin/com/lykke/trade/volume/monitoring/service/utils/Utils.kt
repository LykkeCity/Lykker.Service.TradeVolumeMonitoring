package com.lykke.trade.volume.monitoring.service.utils

import java.math.BigDecimal
import java.math.BigDecimal.ROUND_HALF_UP

val MAX_SCALE_BIGDECIMAL_OPERATIONS = 100

fun equalsIgnoreScale(first: BigDecimal, second: BigDecimal): Boolean {
    return first === second || first.compareTo(second) == 0
}

fun divideWithMaxScale(dividend: BigDecimal, divisor: BigDecimal): BigDecimal {
    return dividend.divide(divisor, MAX_SCALE_BIGDECIMAL_OPERATIONS, ROUND_HALF_UP)
}