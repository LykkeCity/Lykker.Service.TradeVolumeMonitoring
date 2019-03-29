package com.lykke.trade.volume.monitoring.service.web.controllers

import com.lykke.trade.volume.monitoring.service.isalive.IsAliveResponseGetter
import com.lykke.trade.volume.monitoring.service.web.dto.IsAliveResponseDto
import com.lykke.trade.volume.monitoring.service.web.dto.MonitoringStatsDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.apache.http.HttpStatus
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
            MonitoringStatsDto(vmCpuLoad = it.vmCpuLoad,
                    totalCpuLoad = it.totalCpuLoad,
                    totalMemory = it.totalMemory,
                    freeMemory = it.freeMemory,
                    maxHeap = it.maxHeap,
                    totalHeap = it.totalHeap,
                    freeHeap = it.freeHeap,
                    totalSwap = it.totalSwap,
                    freeSwap = it.freeSwap,
                    threadsCount = it.threadsCount)
        }
        val isAliveResponseDto = IsAliveResponseDto(isAliveResponse.version, monitoringStatsDto)
        LOGGER.debug("Got isAlive request")
        return ResponseEntity
                .status(if (isAliveResponse.ok) HttpStatus.SC_OK else HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .body(isAliveResponseDto)
    }
}
