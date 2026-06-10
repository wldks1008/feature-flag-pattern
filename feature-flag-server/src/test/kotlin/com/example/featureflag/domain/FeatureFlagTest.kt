package com.example.featureflag.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class FeatureFlagTest {
    @Test
    fun `boolean flag accepts true or false`() {
        val flag = FeatureFlag.create(
            key = "alarm.sms.enabled",
            type = FeatureFlagType.BOOLEAN,
            value = "true",
            defaultValue = "false",
            description = "SMS enabled"
        )

        assertThat(flag.value).isEqualTo("true")
    }

    @Test
    fun `number flag rejects non numeric value`() {
        assertThatThrownBy {
            FeatureFlag.create(
                key = "alarm.rate-limit-per-second",
                type = FeatureFlagType.NUMBER,
                value = "fast",
                defaultValue = "10",
                description = "Rate limit"
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("NUMBER")
    }

    @Test
    fun `string list flag trims comma separated values`() {
        val flag = FeatureFlag.create(
            key = "alarm.new-template.target-users",
            type = FeatureFlagType.STRING_LIST,
            value = "user-1, user-2",
            defaultValue = "",
            description = "New template users"
        )

        assertThat(flag.stringListValue()).containsExactly("user-1", "user-2")
    }
}
