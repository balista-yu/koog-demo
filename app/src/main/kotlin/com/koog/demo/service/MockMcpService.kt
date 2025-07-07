package com.koog.demo.service

import com.koog.demo.mcp.McpTool
import com.koog.demo.mcp.McpResource
import kotlinx.serialization.json.JsonObject
import mu.KotlinLogging
import org.springframework.stereotype.Service

/**
 * Mock MCP service for testing when MCP server is not available
 */
@Service
class MockMcpService {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Get mock tools
     */
    fun getAvailableTools(): List<McpTool> {
        return listOf(
            McpTool(
                name = "weather",
                description = "Get weather information for a location",
                inputSchema = null
            ),
            McpTool(
                name = "calculator",
                description = "Perform mathematical calculations",
                inputSchema = null
            ),
            McpTool(
                name = "data-analysis",
                description = "Analyze and summarize data",
                inputSchema = null
            )
        )
    }
    
    /**
     * Get mock resources
     */
    fun getAvailableResources(): List<McpResource> {
        return listOf(
            McpResource(
                uri = "koog://weather",
                name = "Weather Data",
                description = "Current weather information",
                mimeType = "application/json"
            ),
            McpResource(
                uri = "koog://system-status",
                name = "System Status",
                description = "Current system status and metrics",
                mimeType = "application/json"
            )
        )
    }
    
    /**
     * Mock weather service
     */
    fun getWeather(location: String): String {
        logger.info("🌤️ Mock weather request for: $location")
        
        val mockWeatherData = when (location.lowercase()) {
            "tokyo", "東京" -> "東京の天気: 晴れ, 気温: 25°C, 湿度: 60%"
            "osaka", "大阪" -> "大阪の天気: 曇り, 気温: 23°C, 湿度: 65%"
            "kyoto", "京都" -> "京都の天気: 雨, 気温: 20°C, 湿度: 80%"
            "new york", "ny" -> "New York weather: Sunny, 22°C, Humidity: 55%"
            "london" -> "London weather: Cloudy, 18°C, Humidity: 70%"
            else -> "$location の天気: データが見つかりません。東京、大阪、京都、New York、Londonをお試しください。"
        }
        
        return mockWeatherData
    }
    
    /**
     * Mock calculator service
     */
    fun calculate(expression: String): String {
        logger.info("🔢 Mock calculation request: $expression")
        
        return try {
            // Simple calculation parser for basic operations
            val result = when {
                expression.contains("+") -> {
                    val parts = expression.split("+").map { it.trim().toDouble() }
                    parts.sum()
                }
                expression.contains("-") -> {
                    val parts = expression.split("-").map { it.trim().toDouble() }
                    parts.reduce { acc, num -> acc - num }
                }
                expression.contains("*") -> {
                    val parts = expression.split("*").map { it.trim().toDouble() }
                    parts.reduce { acc, num -> acc * num }
                }
                expression.contains("/") -> {
                    val parts = expression.split("/").map { it.trim().toDouble() }
                    parts.reduce { acc, num -> acc / num }
                }
                else -> expression.trim().toDouble()
            }
            
            "$expression = $result"
        } catch (e: Exception) {
            "計算エラー: $expression は有効な数式ではありません。例: 2+2, 10-3, 5*4, 20/4"
        }
    }
    
    /**
     * Mock resource reader
     */
    fun readResource(uri: String): String {
        logger.info("📖 Mock resource read request: $uri")
        
        return when (uri) {
            "koog://weather" -> """
                {
                    "current_weather": {
                        "location": "Tokyo",
                        "temperature": 25,
                        "condition": "sunny",
                        "humidity": 60,
                        "timestamp": "${System.currentTimeMillis()}"
                    }
                }
            """.trimIndent()
            
            "koog://system-status" -> """
                {
                    "system_status": {
                        "status": "operational",
                        "uptime": "24h 30m",
                        "memory_usage": "65%",
                        "cpu_usage": "45%",
                        "timestamp": "${System.currentTimeMillis()}"
                    }
                }
            """.trimIndent()
            
            else -> "Resource not found: $uri"
        }
    }
    
    /**
     * Check if MCP server is available (mock always returns false)
     */
    fun isServerAvailable(): Boolean {
        return false
    }
    
    /**
     * Get server status
     */
    fun getServerStatus(): Map<String, Any?> {
        return mapOf(
            "status" to "mock",
            "message" to "Using mock MCP service - real server not available",
            "tools" to getAvailableTools().map { mapOf("name" to it.name, "description" to it.description) },
            "resources" to getAvailableResources().map { mapOf("uri" to it.uri, "name" to it.name) }
        )
    }
}