package com.lykke.trade.volume.monitoring.service.notification

interface MailValidator {
    fun validate(emailAddresses: List<String>, subject: String, textBody: String)
}