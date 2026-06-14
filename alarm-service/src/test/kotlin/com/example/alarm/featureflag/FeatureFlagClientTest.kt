package com.example.alarm.featureflag

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class FeatureFlagClientTest {
    @Test
    fun `returns cached value when present`() {
        val cache = FakeFlagCache(mutableMapOf("feature-flag:alarm.sms.enabled" to "false"))
        val remote = FakeRemoteFlagReader(mapOf("alarm.sms.enabled" to "true"))
        val client = CachedFeatureFlagClient(
            cache,
            remote,
            FeatureFlagProperties("http://localhost:8081", Duration.ofSeconds(5), "feature-flag")
        )

        val result = client.booleanFlag("alarm.sms.enabled", defaultValue = true)

        assertThat(result.value).isFalse()
        assertThat(result.source).isEqualTo(FlagSource.CACHE)
        assertThat(remote.calls).isEqualTo(0)
    }

    @Test
    fun `calls remote and caches value on cache miss`() {
        val cache = FakeFlagCache()
        val remote = FakeRemoteFlagReader(mapOf("alarm.sms.enabled" to "false"))
        val client = CachedFeatureFlagClient(
            cache,
            remote,
            FeatureFlagProperties("http://localhost:8081", Duration.ofSeconds(5), "feature-flag")
        )

        val result = client.booleanFlag("alarm.sms.enabled", defaultValue = true)

        assertThat(result.value).isFalse()
        assertThat(result.source).isEqualTo(FlagSource.REMOTE)
        assertThat(cache.get("feature-flag:alarm.sms.enabled")).isEqualTo("false")
    }

    @Test
    fun `uses default when remote fails`() {
        val cache = FakeFlagCache()
        val remote = FakeRemoteFlagReader(emptyMap(), fail = true)
        val client = CachedFeatureFlagClient(
            cache,
            remote,
            FeatureFlagProperties("http://localhost:8081", Duration.ofSeconds(5), "feature-flag")
        )

        val result = client.booleanFlag("alarm.sms.enabled", defaultValue = true)

        assertThat(result.value).isTrue()
        assertThat(result.source).isEqualTo(FlagSource.DEFAULT)
    }

    private class FakeFlagCache(
        private val values: MutableMap<String, String> = ConcurrentHashMap()
    ) : FlagCache {
        override fun get(key: String): String? = values[key]

        override fun put(key: String, value: String, ttl: Duration) {
            values[key] = value
        }
    }

    private class FakeRemoteFlagReader(
        private val values: Map<String, String>,
        private val fail: Boolean = false
    ) : RemoteFlagReader {
        var calls = 0

        override fun readValue(key: String): String? {
            calls += 1
            if (fail) error("remote failed")
            return values[key]
        }
    }
}
