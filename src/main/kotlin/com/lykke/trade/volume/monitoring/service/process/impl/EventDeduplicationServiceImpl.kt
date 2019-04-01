package com.lykke.trade.volume.monitoring.service.process.impl

import com.lykke.trade.volume.monitoring.service.exception.ApplicationException
import com.lykke.trade.volume.monitoring.service.loader.EventsLoader
import com.lykke.trade.volume.monitoring.service.process.EventDeduplicationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.TaskScheduler
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class EventDeduplicationServiceImpl(private val eventsLoader: EventsLoader,
                                    private val cleanPeriod: Long,
                                    private val taskScheduler: TaskScheduler) : EventDeduplicationService {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EventDeduplicationServiceImpl::class.java.name)
    }

    private val lock = ReentrantLock()
    private var initialized = false
    private var sequenceNumbers: MutableSet<Long> = HashSet()
    private var prevSequenceNumbers: Set<Long>? = null

    @Synchronized
    fun init() {
        if (initialized) {
            throw ApplicationException("Already initialized")
        }
        val loadedSequenceNumbers = eventsLoader.loadEvents().map { it.sequenceNumber }
        lock.withLock {
            sequenceNumbers.addAll(loadedSequenceNumbers)
            LOGGER.info("Initialized with events: ${sequenceNumbers.size}")
        }
        taskScheduler.scheduleAtFixedRate(::clean,
                ZonedDateTime.now().toInstant().plusMillis(cleanPeriod),
                Duration.ofMillis(cleanPeriod))
        initialized = true
    }

    override fun checkAndAdd(sequenceNumber: Long): Boolean {
        lock.withLock {
            val isNotDuplicate = !isDuplicate(sequenceNumber)
            if (isNotDuplicate) {
                sequenceNumbers.add(sequenceNumber)
            }
            return isNotDuplicate
        }
    }

    private fun isDuplicate(sequenceNumber: Long): Boolean {
        return sequenceNumbers.contains(sequenceNumber) || prevSequenceNumbers?.contains(sequenceNumber) == true
    }

    private fun clean() {
        val newSequenceNumbers = HashSet<Long>()
        lock.withLock {
            prevSequenceNumbers = sequenceNumbers
            sequenceNumbers = newSequenceNumbers
        }
    }

}