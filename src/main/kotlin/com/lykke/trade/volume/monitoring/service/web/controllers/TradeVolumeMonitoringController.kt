package com.lykke.trade.volume.monitoring.service.web.controllers

import com.lykke.client.accounts.ClientAccountsCache
import com.lykke.trade.volume.monitoring.service.cache.AssetsCache
import com.lykke.trade.volume.monitoring.service.cache.SentNotificationsCache
import com.lykke.trade.volume.monitoring.service.cache.TradeVolumeCache
import com.lykke.trade.volume.monitoring.service.entity.ClientTradeVolume
import com.lykke.trade.volume.monitoring.service.web.dto.ClientTradeVolumeDto
import com.lykke.trade.volume.monitoring.service.web.dto.TradeVolumeLimitNotificationDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.IllegalArgumentException
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/trade/volume")
@Api(description = "API to get client trade volume", tags = ["Trade volume"])
class TradeVolumeMonitoringController {

    @Autowired
    private lateinit var tradeVolumeCache: TradeVolumeCache

    @Autowired
    private lateinit var sentNotificationsCache: SentNotificationsCache

    @Autowired
    private lateinit var clientAccountsCache: ClientAccountsCache

    @Autowired
    private lateinit var assetsCache: AssetsCache

    @GetMapping("client/{clientId}")
    @ApiOperation("Get trade volume for client")
    @ApiResponses(
            ApiResponse(code = 200, message = "Success"),
            ApiResponse(code = 400, message = "Client or asset does not exist"),
            ApiResponse(code = 500, message = "Internal server error occurred")
    )
    fun getTradeVolumes(@PathVariable("clientId") clientId: String,
                        @RequestParam("assetId", required = false) assetId: String?): List<ClientTradeVolumeDto> {
        validate(clientId, assetId)

        return if (assetId != null) {
            val tradeVolumeForLastPeriod = tradeVolumeCache.getTradeVolumeForLastPeriod(clientId, assetId)
                    ?: return emptyList()
            return listOf(toClientTradeVolumeDto(tradeVolumeForLastPeriod))
        } else {
            tradeVolumeCache.getTradeVolumesForLastPeriod(clientId).map(::toClientTradeVolumeDto)
        }
    }

    @GetMapping("notifications")
    @ApiOperation("Get trade volume limit notifications for last trade period")
    @ApiResponses(
            ApiResponse(code = 200, message = "Success"),
            ApiResponse(code = 500, message = "Internal server error occurred")
    )
    fun getTradeVolumeNotifications(): List<TradeVolumeLimitNotificationDto> {
        return sentNotificationsCache.getAllNotificationsForLastTradePeriod().map {
            TradeVolumeLimitNotificationDto(clientId = it.clientId,
                    assetId = it.assetId,
                    timestamp = it.timestamp)
        }
    }

    private fun validate(clientId: String, assetId: String?) {
        if (assetId != null && assetsCache.getAsset(assetId) == null) {
            throw IllegalArgumentException("Asset with id: $assetId doest not exist")
        }

        if (clientAccountsCache.getWalletsByClientId(clientId).isEmpty()) {
            throw IllegalArgumentException("Client with id: $clientId, does not exist")
        }
    }

    private fun toClientTradeVolumeDto(clientTradeVolume: ClientTradeVolume): ClientTradeVolumeDto {
        return clientTradeVolume.let {
            ClientTradeVolumeDto(it.clientId,
                    it.assetId,
                    it.volume,
                    it.timestamp)
        }
    }

    @ExceptionHandler
    private fun handleIllegalArguments(request: HttpServletRequest, exception: IllegalArgumentException): ResponseEntity<String> {
        return ResponseEntity(exception.message ?: "", HttpStatus.BAD_REQUEST)
    }
}