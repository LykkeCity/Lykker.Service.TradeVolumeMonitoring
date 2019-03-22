package com.lykke.trade.volume.monitoring.service

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

private val LOGGER = LoggerFactory.getLogger("AppStarter")

fun main(args: Array<String>) {
    try {
        runApplication<Application>(*args)
    } catch (e: Throwable) {
        LOGGER.error("Unable to start app: ${e.message}" , e)
        System.exit(1)
    }
}
