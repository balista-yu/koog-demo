package com.koog.demo.service

import com.koog.demo.mcp.McpClient
import com.koog.demo.mcp.McpTool
import com.koog.demo.mcp.McpResource
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import mu.KotlinLogging
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

/**
 * Service for integrating MCP server with Koog application
 */
@Service
class McpIntegrationService(
    private val mcpClient: McpClient,
    private val mockMcpService: MockMcpService
) {
    private val logger = KotlinLogging.logger {}
    private var useMockService = true
    
    @PostConstruct
    fun initialize() {
        runBlocking {
            try {
                logger.info("🔌 Initializing MCP integration...")
                
                // For now, we'll disable automatic MCP server startup
                // and rely on the MCP server being available externally
                logger.info("📋 MCP integration initialized (server startup disabled)")
                
            } catch (e: Exception) {
                logger.error("❌ Error initializing MCP integration", e)
            }
        }
    }
    
    @PreDestroy
    fun cleanup() {
        runBlocking {
            try {
                logger.info("🔌 Cleaning up MCP integration...")
                mcpClient.stopServer()
            } catch (e: Exception) {
                logger.error("Error cleaning up MCP integration", e)
            }
        }
    }
    
    /**
     * Get weather information via MCP
     */
    suspend fun getWeatherViaMcp(location: String): String {
        return if (useMockService) {
            mockMcpService.getWeather(location)
        } else {
            try {
                logger.info("🌤️ Getting weather for $location via MCP")
                
                val arguments = JsonObject(mapOf(
                    "location" to JsonPrimitive(location)
                ))
                
                mcpClient.callTool("weather", arguments)
            } catch (e: Exception) {
                logger.error("Error getting weather via MCP", e)
                "Error: ${e.message}"
            }
        }
    }
    
    /**
     * Perform calculation via MCP
     */
    suspend fun calculateViaMcp(expression: String): String {
        return if (useMockService) {
            mockMcpService.calculate(expression)
        } else {
            try {
                logger.info("🔢 Calculating '$expression' via MCP")
                
                val arguments = JsonObject(mapOf(
                    "expression" to JsonPrimitive(expression)
                ))
                
                mcpClient.callTool("calculator", arguments)
            } catch (e: Exception) {
                logger.error("Error calculating via MCP", e)
                "Error: ${e.message}"
            }
        }
    }
    
    /**
     * Get available MCP tools
     */
    suspend fun getAvailableTools(): List<McpTool> {
        return if (useMockService) {
            mockMcpService.getAvailableTools()
        } else {
            try {
                mcpClient.listTools()
            } catch (e: Exception) {
                logger.error("Error getting available tools", e)
                emptyList()
            }
        }
    }
    
    /**
     * Get available MCP resources
     */
    suspend fun getAvailableResources(): List<McpResource> {
        return if (useMockService) {
            mockMcpService.getAvailableResources()
        } else {
            try {
                mcpClient.listResources()
            } catch (e: Exception) {
                logger.error("Error getting available resources", e)
                emptyList()
            }
        }
    }
    
    /**
     * Read MCP resource
     */
    suspend fun readResource(uri: String): String {
        return if (useMockService) {
            mockMcpService.readResource(uri)
        } else {
            try {
                logger.info("📖 Reading resource: $uri")
                mcpClient.readResource(uri)
            } catch (e: Exception) {
                logger.error("Error reading resource", e)
                "Error: ${e.message}"
            }
        }
    }
    
    /**
     * Get MCP server status
     */
    suspend fun getServerStatus(): Map<String, Any?> {
        return if (useMockService) {
            mockMcpService.getServerStatus()
        } else {
            try {
                val tools = mcpClient.listTools()
                val resources = mcpClient.listResources()
                
                mapOf(
                    "status" to "active",
                    "tools" to tools.map { mapOf("name" to it.name, "description" to it.description) },
                    "resources" to resources.map { mapOf("uri" to it.uri, "name" to it.name) }
                )
            } catch (e: Exception) {
                mapOf(
                    "status" to "error",
                    "message" to (e.message ?: "Unknown error")
                )
            }
        }
    }
}