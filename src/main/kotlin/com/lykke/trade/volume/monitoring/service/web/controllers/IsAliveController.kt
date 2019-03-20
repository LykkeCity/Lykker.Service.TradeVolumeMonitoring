package com.lykke.trade.volume.monitoring.service.web.controllers

import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponseGetter
import com.lykke.trade.volume.monitoring.service.web.dto.IsAliveResponseDto
import com.lykke.trade.volume.monitoring.service.web.dto.MonitoringStatsDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(description = "API to check alive status", tags = ["IsAlive"])
class IsAliveController(private val isAliveResponseGetter: IsAliveResponseGetter) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(IsAliveController::class.java.name)
    }

    @GetMapping("/api/IsAlive", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation("Get alive status")
    fun isAlive(): ResponseEntity<IsAliveResponseDto> {
        val isAliveResponse = isAliveResponseGetter.getResponse()
        val monitoringStatsDto = isAliveResponse.monitoringStats?.let {
            MonitoringStatsDto(VmCpuLoad = it.vmCpuLoad,
                    TotalCpuLoad = it.totalCpuLoad,
                    TotalMemory = it.totalMemory,
                    FreeMemory = it.freeMemory,
                    MaxHeap = it.maxHeap,
                    TotalHeap = it.totalHeap,
                    FreeHeap = it.freeHeap,
                    TotalSwap = it.totalSwap,
                    FreeSwap = it.freeSwap,
                    ThreadsCount = it.threadsCount)
        }
        val isAliveResponseDto = IsAliveResponseDto(isAliveResponse.version, monitoringStatsDto)
        LOGGER.debug("Got isAlive request")
        return ResponseEntity.status(isAliveResponse.responseCode).body(isAliveResponseDto)
    }
}
