package com.example.alarm.alarm

import com.example.alarm.featureflag.FeatureFlagClient
import com.example.alarm.featureflag.FlagEvaluation
import com.example.alarm.featureflag.FlagSource
import com.example.alarm.ratelimit.SimpleRateLimiter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AlarmProcessorTest {
    @Test
    fun `skips sms when sms flag is disabled`() {
        val processor = AlarmProcessor(
            featureFlagClient = FakeFeatureFlagClient(smsEnabled = false),
            rateLimiter = SimpleRateLimiter()
        )

        val result = processor.process(AlarmRequest("user-1", AlarmChannel.SMS, "hello"))

        assertThat(result.status).isEqualTo(AlarmStatus.SKIPPED)
        assertThat(result.reason).isEqualTo("SMS_DISABLED_BY_FEATURE_FLAG")
    }

    @Test
    fun `uses new template for targeted user`() {
        val processor = AlarmProcessor(
            featureFlagClient = FakeFeatureFlagClient(targetUsers = listOf("user-1")),
            rateLimiter = SimpleRateLimiter()
        )

        val result = processor.process(AlarmRequest("user-1", AlarmChannel.SMS, "hello"))

        assertThat(result.status).isEqualTo(AlarmStatus.SENT)
        assertThat(result.renderedMessage).isEqualTo("[NEW] hello")
    }

    @Test
    fun `rate limits when configured limit is exceeded`() {
        val processor = AlarmProcessor(
            featureFlagClient = FakeFeatureFlagClient(rateLimit = 1),
            rateLimiter = SimpleRateLimiter()
        )

        val first = processor.process(AlarmRequest("user-1", AlarmChannel.SMS, "hello"))
        val second = processor.process(AlarmRequest("user-2", AlarmChannel.SMS, "hello"))

        assertThat(first.status).isEqualTo(AlarmStatus.SENT)
        assertThat(second.status).isEqualTo(AlarmStatus.RATE_LIMITED)
    }

    private class FakeFeatureFlagClient(
        private val smsEnabled: Boolean = true,
        private val rateLimit: Int = 10,
        private val targetUsers: List<String> = emptyList()
    ) : FeatureFlagClient {
        override fun booleanFlag(key: String, defaultValue: Boolean): FlagEvaluation<Boolean> =
            FlagEvaluation(key, smsEnabled, FlagSource.REMOTE)

        override fun numberFlag(key: String, defaultValue: Int): FlagEvaluation<Int> =
            FlagEvaluation(key, rateLimit, FlagSource.REMOTE)

        override fun stringListFlag(key: String, defaultValue: List<String>): FlagEvaluation<List<String>> =
            FlagEvaluation(key, targetUsers, FlagSource.REMOTE)
    }
}
