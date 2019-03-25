package com.lykke.trade.volume.monitoring.service.notification.impl

import com.lykke.trade.volume.monitoring.service.notification.MailNotificationService
import org.apache.commons.lang3.StringUtils
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException

@Service
class MailNotificationServiceImpl: MailNotificationService {
    override fun sendMail(email: String, subject: String, textBody: String) {
        validate(email, subject, textBody)


    }

    private fun validate(email: String, subject: String, textBody: String) {
        if (!EmailValidator.getInstance().isValid(email)) {
            throw IllegalArgumentException("Mail mail address is invalid, mail can not be sent, subject $subject")
        }

        if (StringUtils.isEmpty(subject)) {
            throw IllegalArgumentException("Mail subject can not be empty")
        }

        if (StringUtils.isEmpty(textBody)) {
            throw IllegalArgumentException("Body of the mail can not be empty")
        }
    }
}