package com.koog.demo.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val name: String? = null,
    val main: Main? = null,
    val weather: List<Weather>? = null,
    val cod: Int? = null,
    val message: String? = null
) {
    @Serializable
    data class Main(
        val temp: Double,
        val feels_like: Double,
        val humidity: Int
    )

    @Serializable
    data class Weather(
        val main: String,
        val description: String
    )
}