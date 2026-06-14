package com.example.alarm.featureflag

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "feature-flag")
data class FeatureFlagProperties(
    val baseUrl: String,
    val cacheTtl: Duration,
    val cacheKeyPrefix: String
)
