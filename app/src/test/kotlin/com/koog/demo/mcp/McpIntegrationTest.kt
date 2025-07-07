package com.koog.demo.mcp

import com.koog.demo.service.McpIntegrationService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for MCP functionality
 * These tests require the MCP server to be running
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["app.mcp.enabled=true"])
@EnabledIfEnvironmentVariable(named = "RUN_MCP_TESTS", matches = "true")
class McpIntegrationTest {

    @Autowired
    private lateinit var mcpIntegrationService: McpIntegrationService

    @Test
    fun `test MCP tools are available`() = runBlocking {
        // Given
        val tools = mcpIntegrationService.getAvailableTools()
        
        // Then
        assertNotNull(tools)
        assertTrue(tools.isNotEmpty(), "MCP tools should be available")
        assertTrue(tools.any { it.name == "weather" }, "Weather tool should be available")
        assertTrue(tools.any { it.name == "calculator" }, "Calculator tool should be available")
    }

    @Test
    fun `test MCP resources are available`() = runBlocking {
        // Given
        val resources = mcpIntegrationService.getAvailableResources()
        
        // Then
        assertNotNull(resources)
        assertTrue(resources.isNotEmpty(), "MCP resources should be available")
    }

    @Test
    fun `test weather tool via MCP`() = runBlocking {
        // Given
        val location = "Tokyo"
        
        // When
        val result = mcpIntegrationService.getWeatherViaMcp(location)
        
        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Weather result should not be empty")
    }

    @Test
    fun `test calculator tool via MCP`() = runBlocking {
        // Given
        val expression = "2 + 2"
        
        // When
        val result = mcpIntegrationService.calculateViaMcp(expression)
        
        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty(), "Calculator result should not be empty")
    }

    @Test
    fun `test read MCP resource`() = runBlocking {
        // Given
        val uri = "koog://weather"
        
        // When
        val content = mcpIntegrationService.readResource(uri)
        
        // Then
        assertNotNull(content)
        assertTrue(content.isNotEmpty(), "Resource content should not be empty")
    }
}