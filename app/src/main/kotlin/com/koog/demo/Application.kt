package com.koog.demo

import com.koog.demo.config.WeatherProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(WeatherProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}