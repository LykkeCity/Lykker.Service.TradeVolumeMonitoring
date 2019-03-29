package com.lykke.trade.volume.monitoring.service.notification

import com.lykke.trade.volume.monitoring.service.notification.impl.SendMailRequestBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.IllegalArgumentException

class SendMailRequestBuilderTest {

    private lateinit var sendMailRequestBuilder: SendMailRequestBuilder

    @Before
    fun init() {
        sendMailRequestBuilder = SendMailRequestBuilder("%mailAddress_%senderAddress_%subject_%body")
    }

    @Test(expected = IllegalArgumentException::class)
    fun notAllParametersProvided() {
        sendMailRequestBuilder.setSenderAddress("sender@test.com")
                .setMailAddress("receiver@test.com")
                .setBody("test body")
                .build()
    }

    @Test
    fun allParamsProvided() {
        val subject = "subject"
        val address = "receiver@test.com"
        val senderAddress = "sender@test.com"
        val body = "test body"
        val message = sendMailRequestBuilder.setSenderAddress(senderAddress)
                .setMailAddress(address)
                .setSubject(subject)
                .setBody(body)
                .build()

        assertEquals("${address}_${senderAddress}_${subject}_$body", message)
    }
}