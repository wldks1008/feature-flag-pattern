package com.example.alarm.alarm

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/alarms")
class AlarmController(
    private val alarmProcessor: AlarmProcessor
) {
    @PostMapping
    fun send(@RequestBody request: AlarmRequest): AlarmResponse =
        alarmProcessor.process(request)
}
