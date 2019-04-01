package com.lykke.trade.volume.monitoring.service.web.controllers

import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.web.dto.TradeVolumeLimitNotificationDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/trade/volume")
@Api(description = "API to check alive status", tags = ["IsAlive"])
class TradeVolumeMonitoringController {

    @Autowired
    private lateinit var tradeVolumeCache: TradeVolumeCache

    @Autowired
    private lateinit var sentNotificationsCache: SentNotificationsCache

    @GetMapping("client/{clientId}")
    @ApiOperation("Get trade volume for client")
    @ApiResponses(
            ApiResponse(code = 200, message = "Success"),
            ApiResponse(code = 404, message = "Client or asset does not exist"),
            ApiResponse(code = 500, message = "Internal server error occurred")
    )
    fun getTradeVolume(@PathVariable("clientId") clientId: String,
                       @RequestParam("assetId", required = false) assetId: String?) {
    }

    @GetMapping("notifications")
    @ApiOperation("Get trade volume limit notifications for last trade period")
    @ApiResponses(
            ApiResponse(code = 200, message = "Success"),
            ApiResponse(code = 500, message = "Internal server error occurred")
    )
    fun getTradeVolumeNotifications(): List<TradeVolumeLimitNotificationDto> {
        return sentNotificationsCache.getAllNotificationsForLastTradePeriod().map {
            TradeVolumeLimitNotificationDto(clientId =  it.clientId,
                    assetId =  it.assetId,
                    timestamp =  it.timestamp)
        }
    }
}