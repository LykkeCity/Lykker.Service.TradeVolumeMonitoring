package com.lykke.trade.volume.monitoring.service.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import io.swagger.annotations.ApiModel

@ApiModel("IsAliveResponse")
class IsAliveResponseDto(@SerializedName("Version")
                         @JsonProperty("Version")
                         val version: String,
                         @SerializedName("MonitoringStats")
                         @JsonProperty("MonitoringStats")
                         val monitoringStats: MonitoringStatsDto?)

