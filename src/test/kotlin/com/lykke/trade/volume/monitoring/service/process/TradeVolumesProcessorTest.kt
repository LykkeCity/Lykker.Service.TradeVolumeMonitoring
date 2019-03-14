package com.lykke.trade.volume.monitoring.service.process

import com.lykke.trade.volume.monitoring.service.entity.EventTradeVolumesWrapper
import com.lykke.trade.volume.monitoring.service.entity.TradeVolume
import com.lykke.trade.volume.monitoring.service.process.impl.TradeVolumesProcessorImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.math.BigDecimal

class TradeVolumesProcessorTest {

    private lateinit var processor: TradeVolumesProcessor

    @Before
    fun setUp() {
        val converter = Mockito.mock(AssetVolumeConverter::class.java)
        Mockito.`when`(converter.convert("Asset1", BigDecimal.valueOf(5), "TargetAsset"))
                .thenReturn(BigDecimal.valueOf(50), BigDecimal.valueOf(60))
        processor = TradeVolumesProcessorImpl("TargetAsset", converter)
    }

    @Test
    fun testProcess() {
        processor.process(EventTradeVolumesWrapper("MessageId",
                listOf(
                        TradeVolume("wallet1", "Asset1", BigDecimal.valueOf(5)),
                        TradeVolume("wallet1", "TargetAsset", BigDecimal.valueOf(15)),
                        TradeVolume("wallet2", "Asset1", BigDecimal.valueOf(5)),
                        TradeVolume("wallet2", "TargetAsset", BigDecimal.valueOf(20))
                )))

        // todo assertion
    }
}