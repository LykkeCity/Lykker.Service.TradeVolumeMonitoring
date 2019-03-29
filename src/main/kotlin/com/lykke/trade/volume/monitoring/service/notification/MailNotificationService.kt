package com.lykke.trade.volume.monitoring.service.notification

interface MailNotificationService {
    fun sendMail(emailAddresses: List<String>, subject: String, textBody: String)
}