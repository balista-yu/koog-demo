package com.koog.demo.controller

import com.koog.demo.dto.ChatRequest
import com.koog.demo.dto.ChatResponse
import com.koog.demo.service.KoogAgentService
import com.koog.demo.service.McpIntegrationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/koog")
@CrossOrigin(origins = ["http://localhost:3000"])
class KoogController(
    private val koogAgentService: KoogAgentService,
    private val mcpIntegrationService: McpIntegrationService
) {

    @PostMapping("/chat")
    suspend fun chat(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        return try {
            val response = koogAgentService.chatWithAgent(request)
            ResponseEntity.ok(ChatResponse(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                ChatResponse("エラーが発生しました: ${e.message}")
            )
        }
    }

    @PostMapping("/workflow")
    suspend fun processWorkflow(@RequestBody request: Map<String, String>): ResponseEntity<ChatResponse> {
        val task = request["task"] ?: return ResponseEntity.badRequest().body(
            ChatResponse("タスクが指定されていません")
        )

        return try {
            val response = koogAgentService.processComplexTask(task)
            ResponseEntity.ok(ChatResponse(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                ChatResponse("エラーが発生しました: ${e.message}")
            )
        }
    }
    
    /**
     * Enhanced chat with MCP integration
     */
    @PostMapping("/chat-with-mcp")
    suspend fun chatWithMcp(@RequestBody request: ChatRequest): ResponseEntity<ChatResponse> {
        return try {
            // Check if the request can be handled by MCP tools
            val message = request.message.lowercase()
            
            val response = when {
                message.contains("weather") || message.contains("天気") -> {
                    val location = extractLocation(message)
                    if (location.isNotEmpty()) {
                        val mcpResult = mcpIntegrationService.getWeatherViaMcp(location)
                        "MCP経由で天気情報を取得: $mcpResult"
                    } else {
                        koogAgentService.chatWithAgent(request)
                    }
                }
                message.contains("calculate") || message.contains("計算") -> {
                    val expression = extractExpression(message)
                    if (expression.isNotEmpty()) {
                        val mcpResult = mcpIntegrationService.calculateViaMcp(expression)
                        "MCP経由で計算結果: $mcpResult"
                    } else {
                        koogAgentService.chatWithAgent(request)
                    }
                }
                else -> koogAgentService.chatWithAgent(request)
            }
            
            ResponseEntity.ok(ChatResponse(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(
                ChatResponse("エラーが発生しました: ${e.message}")
            )
        }
    }
    
    /**
     * Get MCP server status
     */
    @GetMapping("/mcp-status")
    suspend fun getMcpStatus(): ResponseEntity<Map<String, Any?>> {
        return try {
            val status = mcpIntegrationService.getServerStatus()
            ResponseEntity.ok(status)
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "status" to "error",
                "message" to (e.message ?: "Unknown error")
            ))
        }
    }
    
    private fun extractLocation(message: String): String {
        // Simple location extraction - in production, use NLP
        val patterns = listOf(
            Regex("weather.*?in\\s+(\\w+)", RegexOption.IGNORE_CASE),
            Regex("天気.*?([\\u4e00-\\u9faf]+)", RegexOption.IGNORE_CASE),
            Regex("(\\w+).*?weather", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        
        return ""
    }
    
    private fun extractExpression(message: String): String {
        // Simple expression extraction - in production, use NLP
        val patterns = listOf(
            Regex("calculate\\s+(.+)", RegexOption.IGNORE_CASE),
            Regex("計算\\s+(.+)", RegexOption.IGNORE_CASE),
            Regex("([0-9+\\-*/().\\s]+)", RegexOption.IGNORE_CASE)
        )
        
        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                val expr = match.groupValues[1].trim()
                if (expr.matches(Regex("[0-9+\\-*/().\\s]+"))) {
                    return expr
                }
            }
        }
        
        return ""
    }
}