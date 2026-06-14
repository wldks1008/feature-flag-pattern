package com.example.featureflag.api

import com.example.featureflag.domain.FeatureFlag
import com.example.featureflag.domain.FeatureFlagRepository
import com.example.featureflag.domain.FeatureFlagType
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class CreateFeatureFlagRequest(
    val key: String,
    val type: FeatureFlagType,
    val value: String,
    val defaultValue: String,
    val description: String
)

data class UpdateFeatureFlagRequest(
    val value: String
)

@RestController
@RequestMapping("/api/flags")
class FeatureFlagController(
    private val repository: FeatureFlagRepository
) {
    @GetMapping
    fun list(): List<FeatureFlag> =
        repository.findAll()

    @GetMapping("/{key}")
    fun get(@PathVariable key: String): ResponseEntity<FeatureFlag> =
        repository.findByKey(key)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateFeatureFlagRequest): FeatureFlag =
        repository.save(
            FeatureFlag.create(
                key = request.key,
                type = request.type,
                value = request.value,
                defaultValue = request.defaultValue,
                description = request.description
            )
        )

    @PutMapping("/{key}")
    fun update(
        @PathVariable key: String,
        @RequestBody request: UpdateFeatureFlagRequest
    ): ResponseEntity<FeatureFlag> {
        val flag = repository.findByKey(key) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(repository.save(flag.withValue(request.value)))
    }
}
