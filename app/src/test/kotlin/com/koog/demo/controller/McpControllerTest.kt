package com.koog.demo.controller

import com.koog.demo.service.McpIntegrationService
import com.koog.demo.mcp.McpTool
import com.koog.demo.mcp.McpResource
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(McpController::class)
class McpControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var mcpIntegrationService: McpIntegrationService

    @Test
    fun `test get tools endpoint`() {
        // Given
        val mockTools = listOf(
            McpTool("weather", "Get weather information", null),
            McpTool("calculator", "Perform calculations", null)
        )
        
        runBlocking {
            whenever(mcpIntegrationService.getAvailableTools()).thenReturn(mockTools)
        }

        // When & Then
        mockMvc.perform(get("/api/mcp/tools"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("weather"))
            .andExpect(jsonPath("$[1].name").value("calculator"))
    }

    @Test
    fun `test get resources endpoint`() {
        // Given
        val mockResources = listOf(
            McpResource("koog://weather", "Weather Data", "Weather information", "application/json")
        )
        
        runBlocking {
            whenever(mcpIntegrationService.getAvailableResources()).thenReturn(mockResources)
        }

        // When & Then
        mockMvc.perform(get("/api/mcp/resources"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].uri").value("koog://weather"))
            .andExpect(jsonPath("$[0].name").value("Weather Data"))
    }

    @Test
    fun `test get weather endpoint`() {
        // Given
        val location = "Tokyo"
        val expectedResult = "Weather in Tokyo: Sunny, 25°C"
        
        runBlocking {
            whenever(mcpIntegrationService.getWeatherViaMcp(location)).thenReturn(expectedResult)
        }

        // When & Then
        mockMvc.perform(get("/api/mcp/weather")
            .param("location", location))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value(expectedResult))
    }

    @Test
    fun `test calculate endpoint`() {
        // Given
        val expression = "2 + 2"
        val expectedResult = "4"
        
        runBlocking {
            whenever(mcpIntegrationService.calculateViaMcp(expression)).thenReturn(expectedResult)
        }

        // When & Then
        mockMvc.perform(get("/api/mcp/calculate")
            .param("expression", expression))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value(expectedResult))
    }

    @Test
    fun `test call tool endpoint`() {
        // Given
        val toolName = "weather"
        val location = "Tokyo"
        val expectedResult = "Weather in Tokyo: Sunny, 25°C"
        
        runBlocking {
            whenever(mcpIntegrationService.getWeatherViaMcp(location)).thenReturn(expectedResult)
        }

        // When & Then
        mockMvc.perform(post("/api/mcp/tools/{toolName}", toolName)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"location": "$location"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.result").value(expectedResult))
    }
}