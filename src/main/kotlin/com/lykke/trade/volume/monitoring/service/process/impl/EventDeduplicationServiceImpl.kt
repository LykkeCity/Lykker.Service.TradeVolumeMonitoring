package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.entity.ProcessedEvent
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.process.EventDeduplicationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class EventDeduplicationServiceImpl(private val eventsLoader: EventsLoader,
                                    private val cleanPeriod: Long,
                                    private val taskScheduler: TaskScheduler) : EventDeduplicationService {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventDeduplicationServiceImpl::class.java.name)
    }

    private var sequenceNumbers: MutableSet<Long> = HashSet()
    private var prevSequenceNumbers: Set<Long>? = null
    private val lock = ReentrantReadWriteLock()

    fun init() {
        val events = eventsLoader.loadEvents()
        sequenceNumbers.addAll(events.map { it.sequenceNumber })

        taskScheduler.scheduleAtFixedRate(::clean,
                ZonedDateTime.now().toInstant().plusMillis(cleanPeriod),
                Duration.ofMillis(cleanPeriod))

        LOGGER.info("Initialized with events: ${sequenceNumbers.size}")
    }

    override fun isDuplicate(sequenceNumber: Long): Boolean {
        lock.read {
            return sequenceNumbers.contains(sequenceNumber) || prevSequenceNumbers?.contains(sequenceNumber) == true
        }
    }

    override fun addProcessedEvent(processedEvent: ProcessedEvent) {
        lock.write {
            sequenceNumbers.add(processedEvent.eventSequenceNumber)
        }
    }

    private fun clean() {
        lock.write {
            prevSequenceNumbers = sequenceNumbers
            sequenceNumbers = HashSet()
        }
    }

}