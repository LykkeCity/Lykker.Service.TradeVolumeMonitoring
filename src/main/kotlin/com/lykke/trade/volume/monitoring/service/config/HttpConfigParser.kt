package com.lykke.trade.volume.monitoring.service.config

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.annotation.PostConstruct
import javax.naming.ConfigurationException

@Component("Config")
@Profile("default", "!local_config")
class HttpConfigParser(private val environment: Environment): FactoryBean<TradeVolumeConfig> {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger("AppStarter")
    }

    override fun getObjectType(): Class<*> {
        return TradeVolumeConfig::class.java
    }

    override fun getObject(): TradeVolumeConfig {
        return getConfig()
    }

    private fun getConfig(): TradeVolumeConfig {
        val commangLineArgs = environment.getProperty("nonOptionArgs", Array<String>::class.java)

        if (commangLineArgs == null) {
            LOGGER.error("Not enough args. Usage: httpConfigString")
            throw IllegalArgumentException("Not enough args. Usage: httpConfigString")
        }

        return downloadConfig(commangLineArgs[0])
    }

    private fun downloadConfig(httpString: String): TradeVolumeConfig {
        val cfgUrl = URL(httpString)
        val connection = cfgUrl.openConnection()
        val inputStream = BufferedReader(InputStreamReader(connection.inputStream))

        try {
            val response = StringBuilder()
            var inputLine = inputStream.readLine()

            while (inputLine != null) {
                response.append(inputLine)
                inputLine = inputStream.readLine()
            }

            inputStream.close()

            val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create()
            return gson.fromJson(response.toString(), TradeVolumeConfig::class.java)
        } catch (e: Exception) {
            throw ConfigurationException("Unable to read config from $httpString: ${e.message}")
        } finally {
            inputStream.close()
        }
    }

    @PostConstruct
    fun test() {
        println("asdf")
    }
}