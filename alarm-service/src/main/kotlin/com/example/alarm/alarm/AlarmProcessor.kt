package com.example.alarm.alarm

import com.example.alarm.featureflag.FeatureFlagClient
import com.example.alarm.ratelimit.SimpleRateLimiter
import org.springframework.stereotype.Service

@Service
class AlarmProcessor(
    private val featureFlagClient: FeatureFlagClient,
    private val rateLimiter: SimpleRateLimiter
) {
    fun process(request: AlarmRequest): AlarmResponse {
        val smsEnabled = featureFlagClient.booleanFlag("alarm.sms.enabled", defaultValue = true)
        val rateLimit = featureFlagClient.numberFlag("alarm.rate-limit-per-second", defaultValue = 5)
        val targetUsers = featureFlagClient.stringListFlag("alarm.new-template.target-users", defaultValue = emptyList())

        val usedFlags = listOf(
            UsedFlag(smsEnabled.key, smsEnabled.source, smsEnabled.value),
            UsedFlag(rateLimit.key, rateLimit.source, rateLimit.value),
            UsedFlag(targetUsers.key, targetUsers.source, targetUsers.value)
        )

        if (request.channel == AlarmChannel.SMS && !smsEnabled.value) {
            return AlarmResponse(
                status = AlarmStatus.SKIPPED,
                channel = request.channel,
                userId = request.userId,
                renderedMessage = null,
                reason = "SMS_DISABLED_BY_FEATURE_FLAG",
                usedFlags = usedFlags
            )
        }

        if (!rateLimiter.tryAcquire(rateLimit.value)) {
            return AlarmResponse(
                status = AlarmStatus.RATE_LIMITED,
                channel = request.channel,
                userId = request.userId,
                renderedMessage = null,
                reason = "RATE_LIMIT_EXCEEDED",
                usedFlags = usedFlags
            )
        }

        val renderedMessage = if (request.userId in targetUsers.value) {
            "[NEW] ${request.message}"
        } else {
            request.message
        }

        return AlarmResponse(
            status = AlarmStatus.SENT,
            channel = request.channel,
            userId = request.userId,
            renderedMessage = renderedMessage,
            reason = null,
            usedFlags = usedFlags
        )
    }
}
