package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.config.HttpApiConfig
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.ApiClient
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.api.EmailApi
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model.EmailAddressee
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model.EmailMessage
import com.lykke.trade.volume.monitoring.service.loader.http.generated.client.model.EmailSendRequest
import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import com.lykke.trade.volume.monitoring.service.notification.MailValidator

class HttpMailNotificationServiceImpl(httpConfig: HttpApiConfig,
                                      private val validator: MailValidator): MailNotificationService {

    private val client: EmailApi

    init {
        val apiClient = ApiClient()
        apiClient.connectTimeout = httpConfig.timeout
        client = EmailApi(apiClient.setBasePath(httpConfig.baseUrl))
    }


    override fun sendMail(emailAddresses: List<String>, subject: String, textBody: String) {
        validator.validate(emailAddresses, subject, textBody)

        val emailSendRequest = getEmailSendRequest(emailAddresses, subject, textBody)
        client.apiEmailSendPost(emailSendRequest)
    }

    private fun getEmailSendRequest(emailAddresses: List<String>, subject: String, textBody: String): EmailSendRequest {
        val emailSendRequest = EmailSendRequest()
        emailSendRequest.to = emailAddresses.map { address ->
            val emailAddressee = EmailAddressee()
            emailAddressee.emailAddress(address)
        }

        val emailMessage = EmailMessage()
        emailMessage.subject = subject
        emailMessage.textBody = textBody

        emailSendRequest.message = emailMessage
        client.apiEmailSendPost(emailSendRequest)
        return emailSendRequest
    }
}