package com.example.featureflag

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FeatureFlagServerApplication

fun main(args: Array<String>) {
    runApplication<FeatureFlagServerApplication>(*args)
}
