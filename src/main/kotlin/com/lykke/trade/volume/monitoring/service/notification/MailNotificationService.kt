package com.lykke.trade.volume.monitoring.service.notification

interface MailNotificationService {
    fun sendMail(email: String, subject: String, textBody: String)
}