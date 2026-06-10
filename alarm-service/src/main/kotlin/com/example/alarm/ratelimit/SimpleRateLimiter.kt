package com.example.alarm.ratelimit

import org.springframework.stereotype.Component
import java.time.Clock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Component
class SimpleRateLimiter(
    private val clock: Clock = Clock.systemUTC()
) {
    private val currentSecond = AtomicLong(-1)
    private val count = AtomicInteger(0)

    fun tryAcquire(limitPerSecond: Int): Boolean {
        if (limitPerSecond <= 0) return false

        val nowSecond = clock.instant().epochSecond
        if (currentSecond.get() != nowSecond) {
            currentSecond.set(nowSecond)
            count.set(0)
        }

        return count.incrementAndGet() <= limitPerSecond
    }
}
