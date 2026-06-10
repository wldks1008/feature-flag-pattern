package com.example.featureflag.web

import com.example.featureflag.domain.FeatureFlagRepository
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class FeatureFlagAdminController(
    private val repository: FeatureFlagRepository
) {
    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    @ResponseBody
    fun index(): String {
        val rows = repository.findAll().joinToString("\n") { flag ->
            """
            <tr>
              <td>${flag.key}</td>
              <td>${flag.type}</td>
              <td>${flag.defaultValue}</td>
              <td>${flag.description}</td>
              <td>
                <form method="post" action="/admin/flags">
                  <input type="hidden" name="key" value="${flag.key}">
                  <input name="value" value="${flag.value}">
                  <button type="submit">Update</button>
                </form>
              </td>
              <td>${flag.updatedAt}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!doctype html>
        <html>
        <head>
          <meta charset="UTF-8">
          <title>Feature Flags</title>
          <style>
            body { font-family: system-ui, sans-serif; margin: 32px; }
            table { border-collapse: collapse; width: 100%; }
            th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            input { width: 240px; }
            button { cursor: pointer; }
          </style>
        </head>
        <body>
          <h1>Feature Flags</h1>
          <table>
            <thead>
              <tr>
                <th>Key</th><th>Type</th><th>Default</th><th>Description</th><th>Value</th><th>Updated</th>
              </tr>
            </thead>
            <tbody>$rows</tbody>
          </table>
        </body>
        </html>
        """.trimIndent()
    }

    @PostMapping("/admin/flags")
    fun update(@RequestParam key: String, @RequestParam value: String): String {
        val flag = repository.findByKey(key) ?: return "redirect:/"
        repository.save(flag.withValue(value))
        return "redirect:/"
    }
}
