package com.koog.demo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
data class KoogProperties(
    val googleApiKey: String = System.getenv("GOOGLE_API_KEY")
)

@ConfigurationProperties(prefix = "weather.api")
data class WeatherProperties(
    val key: String = "",
    val url: String = "https://api.openweathermap.org/data/2.5/weather"
)