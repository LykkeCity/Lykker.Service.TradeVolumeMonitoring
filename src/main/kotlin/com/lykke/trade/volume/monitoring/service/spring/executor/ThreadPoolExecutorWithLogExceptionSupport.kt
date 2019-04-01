package com.lykke.trade.volume.monitoring.service.spring.executor

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.lykke.utils.logging.MetricsLogger
import com.lykke.utils.logging.ThrottlingLogger
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThreadPoolExecutorWithLogExceptionSupport(corePoolSize: Int,
                                                maxPoolSize: Int,
                                                keepAliveTime: Long,
                                                unit: TimeUnit,
                                                workQueue: BlockingQueue<Runnable>,
                                                defaultThreadNameFormat: String) : ThreadPoolExecutor(corePoolSize,
        maxPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        ThreadFactoryBuilder().setNameFormat(defaultThreadNameFormat).build()) {

    private companion object {
        private val LOGGER = ThrottlingLogger.getLogger(ThreadPoolExecutorWithLogExceptionSupport::class.java.name)
        private val METRICS_LOGGER = MetricsLogger.getLogger()
    }

    override fun afterExecute(r: Runnable?, t: Throwable?) {
        super.afterExecute(r, t)
        var exception = t
        if (t == null && r is Future<*>) {
            try {
                (r as Future<*>).get()
            } catch (ce: CancellationException) {
                exception = ce
            } catch (ee: ExecutionException) {
                exception = ee.cause
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt() // ignore/reset
            }
        }

        if (exception != null) {
            val message = "Unhandled exception occurred in thread: ${Thread.currentThread().name}"
            LOGGER.error(message, exception)
            METRICS_LOGGER.logError(message)
        }
    }
}