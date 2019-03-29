package com.lykke.trade.volume.monitoring.service.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import io.swagger.annotations.ApiModel

@ApiModel("MonitoringStats")
class MonitoringStatsDto(@SerializedName("VmCpuLoad")
                         @JsonProperty("VmCpuLoad")
                         val vmCpuLoad: Double,
                         @SerializedName("TotalCpuLoad")
                         @JsonProperty("TotalCpuLoad")
                         val totalCpuLoad: Double,
                         @SerializedName("TotalMemory")
                         @JsonProperty("TotalMemory")
                         val totalMemory: Long,
                         @SerializedName("FreeMemory")
                         @JsonProperty("FreeMemory")
                         val freeMemory: Long,
                         @SerializedName("MaxHeap")
                         @JsonProperty("MaxHeap")
                         val maxHeap: Long,
                         @SerializedName("TotalHeap")
                         @JsonProperty("TotalHeap")
                         val totalHeap: Long,
                         @SerializedName("FreeHeap")
                         @JsonProperty("FreeHeap")
                         val freeHeap: Long,
                         @SerializedName("TotalSwap")
                         @JsonProperty("TotalSwap")
                         val totalSwap: Long,
                         @SerializedName("FreeSwap")
                         @JsonProperty("FreeSwap")
                         val freeSwap: Long,
                         @SerializedName("ThreadsCount")
                         @JsonProperty("ThreadsCount")
                         val threadsCount: Int)