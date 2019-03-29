package com.lykke.trade.volume.monitoring.service.spring

import com.google.gson.Gson
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.GsonHttpMessageConverter

@Configuration
class WebConfig {

    @Bean
    fun gsonHttpMessageConverter(gson: Gson): GsonHttpMessageConverter {
        val converter = GsonHttpMessageConverter()
        converter.gson = gson
        return converter
    }

}
