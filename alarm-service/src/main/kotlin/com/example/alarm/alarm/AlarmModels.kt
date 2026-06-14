package com.example.alarm.alarm

import com.example.alarm.featureflag.FlagSource

enum class AlarmChannel {
    SMS
}

enum class AlarmStatus {
    SENT,
    SKIPPED,
    RATE_LIMITED
}

data class AlarmRequest(
    val userId: String,
    val channel: AlarmChannel,
    val message: String
)

data class UsedFlag(
    val key: String,
    val source: FlagSource,
    val value: Any
)

data class AlarmResponse(
    val status: AlarmStatus,
    val channel: AlarmChannel,
    val userId: String,
    val renderedMessage: String?,
    val reason: String?,
    val usedFlags: List<UsedFlag>
)
