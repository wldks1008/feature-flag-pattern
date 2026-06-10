package com.example.featureflag.api

import com.example.featureflag.domain.FeatureFlagType
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@SpringBootTest
@AutoConfigureMockMvc
class FeatureFlagControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {
    @Test
    fun `lists initial flags`() {
        mockMvc.get("/api/flags")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].key") { exists() }
            }
    }

    @Test
    fun `gets one flag by key`() {
        mockMvc.get("/api/flags/alarm.sms.enabled")
            .andExpect {
                status { isOk() }
                jsonPath("$.key") { value("alarm.sms.enabled") }
                jsonPath("$.value") { value("true") }
            }
    }

    @Test
    fun `updates flag value`() {
        mockMvc.put("/api/flags/alarm.sms.enabled") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateFeatureFlagRequest(value = "false"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.value") { value("false") }
        }
    }

    @Test
    fun `creates a flag`() {
        val request = CreateFeatureFlagRequest(
            key = "alarm.push.enabled",
            type = FeatureFlagType.BOOLEAN,
            value = "true",
            defaultValue = "true",
            description = "Push enabled"
        )

        mockMvc.post("/api/flags") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.key") { value("alarm.push.enabled") }
        }
    }
}
