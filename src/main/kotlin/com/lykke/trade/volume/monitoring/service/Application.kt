package com.lykke.trade.volume.monitoring.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

private val LOGGER = LoggerFactory.getLogger(Application::class.java.name)

fun main(args: Array<String>) {
    try {
        runApplication<Application>(*args)
    } catch (e: Exception) {
        LOGGER.error(e.message ?: "Unable to start app", e)
        System.exit(1)
    }
}
