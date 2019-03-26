package com.lykke.trade.volume.monitoring.service.notification.impl

import org.apache.commons.lang3.StringUtils
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MessageBuilder(private val format: String) {
    private companion object {
        val CLIENT_ID = "clientId"
        val TRADE_VOLUME_LIMIT = "tradeVolumeLimit"
        val TARGET_ASSET_ID = "targetAssetId"
        val ASSET_ID = "assetId"
        val TIMESTAMP = "timestamp"

        val REQUIRED_PARAMS = setOf(CLIENT_ID, TRADE_VOLUME_LIMIT, TARGET_ASSET_ID, ASSET_ID, TIMESTAMP)

        val MACROS_PREFIX = "%"
    }

    private val env = HashMap<String, String>()

    fun setTimestamp(timestamp: Date): MessageBuilder {
        env[TIMESTAMP] = ZonedDateTime
                .ofInstant(timestamp.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return this
    }

    fun setClientId(clientId: String): MessageBuilder {
        env[CLIENT_ID] = clientId
        return this
    }

    fun setTradeVolumeLimit(tradeVolumeLimit: Long): MessageBuilder {
        env[TRADE_VOLUME_LIMIT] = tradeVolumeLimit.toString()
        return this
    }

    fun setTargetAssetId(targetAssetId: String): MessageBuilder {
        env[TARGET_ASSET_ID] = targetAssetId
        return this
    }

    fun setAssetId(assetId: String): MessageBuilder {
        env[ASSET_ID] = assetId
        return this
    }

    fun build(): String {
        validate()

        var message = format
        env.forEach { key, value -> message = message.replace(MACROS_PREFIX + key, value) }
        return message
    }

    private fun validate() {
        REQUIRED_PARAMS.forEach { requiredParam ->
            if (StringUtils.isEmpty(env[requiredParam])) {
                throw IllegalArgumentException("Required parameter: $requiredParam is not provided")
            }
        }
    }
}