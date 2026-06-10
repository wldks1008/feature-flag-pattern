package com.example.featureflag.web

import com.example.featureflag.domain.FeatureFlagRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class FeatureFlagAdminController(
    private val repository: FeatureFlagRepository
) {
    @GetMapping("/")
    fun root(): String =
        "redirect:/admin/flags"

    @GetMapping("/admin/flags")
    fun index(model: Model): String {
        model.addAttribute("flags", repository.findAll())
        return "admin/flags"
    }

    @PostMapping("/admin/flags/{key}")
    fun update(@PathVariable key: String, @RequestParam value: String): String {
        val flag = repository.findByKey(key) ?: return "redirect:/"
        repository.save(flag.withValue(value))
        return "redirect:/admin/flags"
    }
}
