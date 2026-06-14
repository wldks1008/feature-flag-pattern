package com.example.featureflag.domain

import java.time.Instant

enum class FeatureFlagType {
    BOOLEAN,
    NUMBER,
    STRING_LIST
}

data class FeatureFlag(
    val key: String,
    val type: FeatureFlagType,
    val value: String,
    val defaultValue: String,
    val description: String,
    val updatedAt: Instant
) {
    init {
        require(key.isNotBlank()) { "key must not be blank" }
        validate(type, value, "value")
        validate(type, defaultValue, "defaultValue")
    }

    fun withValue(newValue: String): FeatureFlag {
        validate(type, newValue, "value")
        return copy(value = normalize(type, newValue), updatedAt = Instant.now())
    }

    fun stringListValue(): List<String> =
        value.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

    companion object {
        fun create(
            key: String,
            type: FeatureFlagType,
            value: String,
            defaultValue: String,
            description: String
        ): FeatureFlag {
            validate(type, value, "value")
            validate(type, defaultValue, "defaultValue")

            return FeatureFlag(
                key = key,
                type = type,
                value = normalize(type, value),
                defaultValue = normalize(type, defaultValue),
                description = description,
                updatedAt = Instant.now()
            )
        }

        private fun validate(type: FeatureFlagType, rawValue: String, field: String) {
            when (type) {
                FeatureFlagType.BOOLEAN ->
                    require(rawValue.equals("true", ignoreCase = true) || rawValue.equals("false", ignoreCase = true)) {
                        "$field for BOOLEAN must be true or false"
                    }

                FeatureFlagType.NUMBER ->
                    require(rawValue.toIntOrNull() != null && rawValue.toInt() >= 0) {
                        "$field for NUMBER must be a non-negative integer"
                    }

                FeatureFlagType.STRING_LIST -> Unit
            }
        }

        private fun normalize(type: FeatureFlagType, rawValue: String): String =
            when (type) {
                FeatureFlagType.BOOLEAN -> rawValue.lowercase()
                FeatureFlagType.NUMBER -> rawValue.toInt().toString()
                FeatureFlagType.STRING_LIST -> rawValue.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString(",")
            }
    }
}
