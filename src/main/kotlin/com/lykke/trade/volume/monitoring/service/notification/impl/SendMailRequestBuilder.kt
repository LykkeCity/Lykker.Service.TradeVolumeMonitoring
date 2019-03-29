package com.lykke.trade.volume.monitoring.service.notification.impl

import org.apache.commons.lang3.StringUtils
import java.lang.IllegalArgumentException

class SendMailRequestBuilder(private val format: String) {

    private companion object {
        val MAIL_ADDRESS = "mailAddress"
        val SENDER_ADDRESS = "senderAddress"
        val SUBJECT = "subject"
        val BODY = "body"

        val MACROS_PREFIX = "%"

        val REQUIRED_PARAMETERS = setOf(MAIL_ADDRESS, SENDER_ADDRESS, SUBJECT, BODY)
    }

    private val env = HashMap<String, String>()

    fun setMailAddress(address: String): SendMailRequestBuilder {
        env[MAIL_ADDRESS] = address
        return this
    }

    fun setSubject(subject: String): SendMailRequestBuilder {
        env[SUBJECT] = subject
        return this
    }

    fun setBody(body: String): SendMailRequestBuilder {
        env[BODY] = body
        return this
    }

    fun setSenderAddress(senderAddress: String): SendMailRequestBuilder {
        env[SENDER_ADDRESS] = senderAddress
        return this
    }

    fun build(): String {
        validate()
        var resultMessage = format

        env.forEach { key, value ->  resultMessage = resultMessage.replace(MACROS_PREFIX + key, value) }

        return resultMessage
    }

    private fun validate() {
        REQUIRED_PARAMETERS.forEach {
            if (StringUtils.isEmpty(env[it])) {
                throw IllegalArgumentException("Required parameter $it is not provided")
            }
        }
    }
}