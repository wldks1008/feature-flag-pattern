package com.example.featureflag.domain

import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

interface FeatureFlagRepository {
    fun findAll(): List<FeatureFlag>
    fun findByKey(key: String): FeatureFlag?
    fun save(flag: FeatureFlag): FeatureFlag
}

@Repository
class InMemoryFeatureFlagRepository : FeatureFlagRepository {
    private val flags = ConcurrentHashMap<String, FeatureFlag>()

    init {
        save(
            FeatureFlag.create(
                key = "alarm.sms.enabled",
                type = FeatureFlagType.BOOLEAN,
                value = "true",
                defaultValue = "true",
                description = "Controls whether SMS alarms are sent."
            )
        )
        save(
            FeatureFlag.create(
                key = "alarm.rate-limit-per-second",
                type = FeatureFlagType.NUMBER,
                value = "5",
                defaultValue = "5",
                description = "Controls local alarm send throughput per second."
            )
        )
        save(
            FeatureFlag.create(
                key = "alarm.new-template.target-users",
                type = FeatureFlagType.STRING_LIST,
                value = "",
                defaultValue = "",
                description = "User IDs that receive the new alarm template."
            )
        )
    }

    override fun findAll(): List<FeatureFlag> =
        flags.values.sortedBy { it.key }

    override fun findByKey(key: String): FeatureFlag? =
        flags[key]

    override fun save(flag: FeatureFlag): FeatureFlag {
        flags[flag.key] = flag
        return flag
    }
}
