package com.example.alarm

import com.example.alarm.featureflag.FeatureFlagProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(FeatureFlagProperties::class)
class AlarmServiceApplication

fun main(args: Array<String>) {
    runApplication<AlarmServiceApplication>(*args)
}
