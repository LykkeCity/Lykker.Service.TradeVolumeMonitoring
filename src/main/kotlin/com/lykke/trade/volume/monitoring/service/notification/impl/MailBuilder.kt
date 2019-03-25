package com.lykke.trade.volume.monitoring.service.notification.impl

import org.apache.commons.lang3.StringUtils
import java.lang.IllegalArgumentException

class MailBuilder(private val format: String) {
    private companion object {
        val CLIENT_ID = "clientId"
        val TRADE_VOLUME_LIMIT = "tradeVolumeLimit"
        val TARGET_ASSET_ID = "targetAssetId"
        val ASSET_ID = "assetId"

        val REQUIRED_PARAMS = setOf(CLIENT_ID, TRADE_VOLUME_LIMIT, TARGET_ASSET_ID, ASSET_ID)

        val MACROS_PREFIX = "%"
    }

    private val env = HashMap<String, String>()

    fun setClientId(clientId: String): MailBuilder {
        env[CLIENT_ID] = clientId
        return this
    }

    fun setTradeVolumeLimit(tradeVolumeLimit: Long): MailBuilder {
        env[TRADE_VOLUME_LIMIT] = tradeVolumeLimit.toString()
        return this
    }

    fun setTargetAssetId(targetAssetId: String): MailBuilder {
        env[TARGET_ASSET_ID] = targetAssetId
        return this
    }

    fun setAssetId(assetId: String): MailBuilder {
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

            if(!isNumber(env[TRADE_VOLUME_LIMIT]!!)) {
                throw IllegalArgumentException("Trade volume limit should be a number")
            }
        }
    }

    private fun isNumber(text: String): Boolean {
        return text.toCharArray().findLast { !Character.isDigit(it) } != null
    }
}