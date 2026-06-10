package com.example.featureflag.web

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FeatureFlagAdminControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    @Test
    fun `renders admin console with type specific controls`() {
        mockMvc.get("/admin/flags")
            .andExpect {
                status { isOk() }
                content { string(containsString("Admin Flag Console")) }
                content { string(containsString("alarm.sms.enabled")) }
                content { string(containsString("""action="/admin/flags/alarm.sms.enabled"""")) }
                content { string(containsString("""name="value"""")) }
                content { string(containsString("Enable")) }
                content { string(containsString("Disable")) }
                content { string(containsString("TTL")) }
            }
    }

    @Test
    fun `updates flag from admin form and redirects to console`() {
        mockMvc.post("/admin/flags/alarm.sms.enabled") {
            param("value", "false")
        }.andExpect {
            status { is3xxRedirection() }
            redirectedUrl("/admin/flags")
        }

        mockMvc.get("/admin/flags")
            .andExpect {
                status { isOk() }
                content { string(containsString("""value="false"""")) }
            }
    }
}
