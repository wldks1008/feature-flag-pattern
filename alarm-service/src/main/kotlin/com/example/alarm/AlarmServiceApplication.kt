package com.example.alarm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AlarmServiceApplication

fun main(args: Array<String>) {
    runApplication<AlarmServiceApplication>(*args)
}
