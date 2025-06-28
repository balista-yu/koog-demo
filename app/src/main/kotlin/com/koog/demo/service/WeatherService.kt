package com.koog.demo.service

import com.koog.demo.config.WeatherProperties
import com.koog.demo.dto.WeatherResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class WeatherService(
    private val weatherProperties: WeatherProperties,
    private val webClient: WebClient
) {
    private val locationMap = mapOf(
        "東京" to "Tokyo",
        "大阪" to "Osaka", 
        "名古屋" to "Nagoya",
        "京都" to "Kyoto",
        "福岡" to "Fukuoka",
        "札幌" to "Sapporo",
        "横浜" to "Yokohama",
        "神戸" to "Kobe",
        "広島" to "Hiroshima",
        "仙台" to "Sendai"
    )
    
    suspend fun getWeather(location: String): String {
        return try {
            println("WeatherService: API key = '${weatherProperties.key}' (length: ${weatherProperties.key.length})")
            if (weatherProperties.key.isEmpty()) {
                return "天気APIキーが設定されていません。"
            }

            val response = try {
                getWeatherFromAPI(location)
            } catch (e: Exception) {
                val englishLocation = locationMap[location]
                if (englishLocation != null) {
                    println("WeatherService: 英語地名で再試行: $location -> $englishLocation")
                    getWeatherFromAPI(englishLocation)
                } else {
                    throw e
                }
            }

            println("WeatherService: API call successful for location: $location")
            println("WeatherService: Response received: $response")

            if (response.cod != null && response.cod != 200) {
                return "OpenWeatherMap APIエラー: ${response.message ?: "不明なエラー"}"
            }

            if (response.name == null || response.main == null || response.weather == null) {
                return "天気データが不完全です。"
            }

            "今日の${response.name}の天気は${response.weather.firstOrNull()?.description ?: "不明"}、" +
            "気温は${response.main.temp.toInt()}度、体感温度は${response.main.feels_like.toInt()}度、" +
            "湿度は${response.main.humidity}%です。"
        } catch (e: Exception) {
            println("WeatherService: Error occurred: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            "天気情報の取得に失敗しました。地名を確認してください（例：東京、大阪、Tokyo、Osaka）"
        }
    }
    
    private suspend fun getWeatherFromAPI(location: String): WeatherResponse {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host("api.openweathermap.org")
                    .path("/data/2.5/weather")
                    .queryParam("q", location)
                    .queryParam("appid", weatherProperties.key)
                    .queryParam("units", "metric")
                    .queryParam("lang", "ja")
                    .build()
            }
            .retrieve()
            .awaitBody<WeatherResponse>()
    }
}