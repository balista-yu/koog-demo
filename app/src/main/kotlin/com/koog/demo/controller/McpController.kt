package com.koog.demo.controller

import com.koog.demo.mcp.McpTool
import com.koog.demo.mcp.McpResource
import com.koog.demo.service.McpIntegrationService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.web.bind.annotation.*

/**
 * REST controller for MCP integration
 */
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = ["http://localhost:3000"])
class McpController(
    private val mcpIntegrationService: McpIntegrationService
) {
    private val logger = KotlinLogging.logger {}
    
    /**
     * Get available MCP tools
     */
    @GetMapping("/tools")
    fun getTools(): List<McpTool> = runBlocking {
        logger.info("📋 Getting available MCP tools")
        mcpIntegrationService.getAvailableTools()
    }
    
    /**
     * Get available MCP resources
     */
    @GetMapping("/resources")
    fun getResources(): List<McpResource> = runBlocking {
        logger.info("📋 Getting available MCP resources")
        mcpIntegrationService.getAvailableResources()
    }
    
    /**
     * Read MCP resource
     */
    @GetMapping("/resources/{uri}")
    fun readResource(@PathVariable uri: String): Map<String, String> = runBlocking {
        logger.info("📖 Reading MCP resource: $uri")
        val content = mcpIntegrationService.readResource(uri)
        mapOf("content" to content)
    }
    
    /**
     * Get weather via MCP
     */
    @GetMapping("/weather")
    fun getWeatherViaMcp(@RequestParam location: String): Map<String, String> = runBlocking {
        logger.info("🌤️ Getting weather for $location via MCP")
        val result = mcpIntegrationService.getWeatherViaMcp(location)
        mapOf("result" to result)
    }
    
    /**
     * Calculate via MCP
     */
    @GetMapping("/calculate")
    fun calculateViaMcp(@RequestParam expression: String): Map<String, String> = runBlocking {
        logger.info("🔢 Calculating '$expression' via MCP")
        val result = mcpIntegrationService.calculateViaMcp(expression)
        mapOf("result" to result)
    }
    
    /**
     * Generic tool call
     */
    @PostMapping("/tools/{toolName}")
    fun callTool(
        @PathVariable toolName: String,
        @RequestBody arguments: Map<String, Any>
    ): Map<String, String> = runBlocking {
        logger.info("🔧 Calling MCP tool: $toolName with arguments: $arguments")
        
        // Convert arguments to JsonObject
        val jsonArguments = kotlinx.serialization.json.JsonObject(
            arguments.mapValues { (_, value) ->
                when (value) {
                    is String -> kotlinx.serialization.json.JsonPrimitive(value)
                    is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                    is Boolean -> kotlinx.serialization.json.JsonPrimitive(value)
                    else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                }
            }
        )
        
        val result = when (toolName) {
            "weather" -> {
                val location = arguments["location"]?.toString() ?: ""
                mcpIntegrationService.getWeatherViaMcp(location)
            }
            "calculator" -> {
                val expression = arguments["expression"]?.toString() ?: ""
                mcpIntegrationService.calculateViaMcp(expression)
            }
            else -> {
                "Tool not supported: $toolName"
            }
        }
        
        mapOf("result" to result)
    }
}