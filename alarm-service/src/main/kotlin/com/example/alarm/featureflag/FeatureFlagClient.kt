package com.example.alarm.featureflag

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Duration

enum class FlagSource {
    CACHE,
    REMOTE,
    DEFAULT
}

data class FlagEvaluation<T>(
    val key: String,
    val value: T,
    val source: FlagSource
)

interface FeatureFlagClient {
    fun booleanFlag(key: String, defaultValue: Boolean): FlagEvaluation<Boolean>
    fun numberFlag(key: String, defaultValue: Int): FlagEvaluation<Int>
    fun stringListFlag(key: String, defaultValue: List<String>): FlagEvaluation<List<String>>
}

interface FlagCache {
    fun get(key: String): String?
    fun put(key: String, value: String, ttl: Duration)
}

interface RemoteFlagReader {
    fun readValue(key: String): String?
}

@Component
class RedisFlagCache(
    private val redisTemplate: StringRedisTemplate
) : FlagCache {
    override fun get(key: String): String? =
        redisTemplate.opsForValue().get(key)

    override fun put(key: String, value: String, ttl: Duration) {
        redisTemplate.opsForValue().set(key, value, ttl)
    }
}

data class RemoteFeatureFlagResponse(
    val key: String,
    val type: String,
    val value: String,
    val defaultValue: String,
    val description: String
)

@Component
class HttpRemoteFlagReader(
    properties: FeatureFlagProperties
) : RemoteFlagReader {
    private val restClient = RestClient.builder()
        .baseUrl(properties.baseUrl)
        .build()

    override fun readValue(key: String): String? =
        runCatching {
            restClient.get()
                .uri("/api/flags/{key}", key)
                .retrieve()
                .body(RemoteFeatureFlagResponse::class.java)
                ?.value
        }.getOrNull()
}

@Component
class CachedFeatureFlagClient(
    private val cache: FlagCache,
    private val remote: RemoteFlagReader,
    private val properties: FeatureFlagProperties
) : FeatureFlagClient {
    override fun booleanFlag(key: String, defaultValue: Boolean): FlagEvaluation<Boolean> {
        val raw = readRaw(key) ?: return FlagEvaluation(key, defaultValue, FlagSource.DEFAULT)
        return FlagEvaluation(key, raw.value.toBooleanStrictOrNull() ?: defaultValue, raw.source)
    }

    override fun numberFlag(key: String, defaultValue: Int): FlagEvaluation<Int> {
        val raw = readRaw(key) ?: return FlagEvaluation(key, defaultValue, FlagSource.DEFAULT)
        return FlagEvaluation(key, raw.value.toIntOrNull() ?: defaultValue, raw.source)
    }

    override fun stringListFlag(key: String, defaultValue: List<String>): FlagEvaluation<List<String>> {
        val raw = readRaw(key) ?: return FlagEvaluation(key, defaultValue, FlagSource.DEFAULT)
        val values = raw.value.split(",").map { it.trim() }.filter { it.isNotBlank() }
        return FlagEvaluation(key, values, raw.source)
    }

    private fun readRaw(key: String): RawFlagValue? {
        val cacheKey = "${properties.cacheKeyPrefix}:$key"
        runCatching { cache.get(cacheKey) }
            .getOrNull()
            ?.let { return RawFlagValue(it, FlagSource.CACHE) }

        val remoteValue = runCatching { remote.readValue(key) }.getOrNull() ?: return null
        runCatching { cache.put(cacheKey, remoteValue, properties.cacheTtl) }
        return RawFlagValue(remoteValue, FlagSource.REMOTE)
    }

    private data class RawFlagValue(
        val value: String,
        val source: FlagSource
    )
}
