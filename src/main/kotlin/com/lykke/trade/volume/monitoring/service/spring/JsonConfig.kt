package com.lykke.trade.volume.monitoring.service.spring

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import springfox.documentation.spring.web.json.Json
import java.lang.reflect.Type


@Configuration
class JsonConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    fun gson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(Json::class.java, SpringfoxJsonToGsonAdapter())
                .create()

    }
}

private class SpringfoxJsonToGsonAdapter : JsonSerializer<Json> {
    override fun serialize(json: Json,
                           type: Type,
                           jsonSerializationContext: JsonSerializationContext): JsonElement {
        return JsonParser().parse(json.value())
    }
}
