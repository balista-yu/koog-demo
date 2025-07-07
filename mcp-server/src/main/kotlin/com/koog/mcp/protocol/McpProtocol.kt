package com.koog.mcp.protocol

import kotlinx.serialization.json.*
import mu.KotlinLogging

/**
 * MCP Protocol Constants and Error Codes
 */
object McpProtocol {
    const val VERSION = "2025-03-26"

    // JSON-RPC Error Codes
    const val PARSE_ERROR = -32700
    const val INVALID_REQUEST = -32600
    const val METHOD_NOT_FOUND = -32601
    const val INVALID_PARAMS = -32602
    const val INTERNAL_ERROR = -32603

    // MCP-specific Error Codes
    const val INVALID_METHOD = -32000
    const val RESOURCE_NOT_FOUND = -32001
    const val TOOL_NOT_FOUND = -32002
    const val PROMPT_NOT_FOUND = -32003
}

/**
 * MCP Message Handler
 */
class McpMessageHandler {
    private val logger = KotlinLogging.logger {}
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun handleMessage(messageStr: String): String? {
        return try {
            logger.debug("📨 Received: $messageStr")

            val messageJson = json.parseToJsonElement(messageStr)

            when {
                messageJson.jsonObject.containsKey("id") -> {
                    if (messageJson.jsonObject.containsKey("method")) {
                        // Request
                        val request = json.decodeFromJsonElement<JsonRpcRequest>(messageJson)
                        handleRequest(request)
                    } else {
                        // Response - normally client handles this
                        logger.warn("⚠️ Received response, but server shouldn't handle responses")
                        null
                    }
                }
                messageJson.jsonObject.containsKey("method") -> {
                    // Notification
                    val notification = json.decodeFromJsonElement<JsonRpcNotification>(messageJson)
                    handleNotification(notification)
                    null
                }
                else -> {
                    createErrorResponse(
                        JsonNull,
                        McpProtocol.INVALID_REQUEST,
                        "Invalid JSON-RPC message"
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("❌ Error handling message: ${e.message}", e)
            createErrorResponse(
                JsonNull,
                McpProtocol.PARSE_ERROR,
                "Parse error: ${e.message}"
            )
        }
    }

    private suspend fun handleRequest(request: JsonRpcRequest): String {
        logger.info("🔄 Handling request: ${request.method}")

        return when (request.method) {
            "initialize" -> handleInitialize(request)
            "tools/list" -> handleToolsList(request)
            "tools/call" -> handleToolsCall(request)
            "resources/list" -> handleResourcesList(request)
            "resources/read" -> handleResourcesRead(request)
            "prompts/list" -> handlePromptsList(request)
            "prompts/get" -> handlePromptsGet(request)
            else -> {
                createErrorResponse(
                    request.id,
                    McpProtocol.METHOD_NOT_FOUND,
                    "Method not found: ${request.method}"
                )
            }
        }
    }

    private suspend fun handleNotification(notification: JsonRpcNotification) {
        logger.info("📢 Handling notification: ${notification.method}")

        when (notification.method) {
            "notifications/initialized" -> {
                logger.info("✅ Client initialized")
            }
            "logging/setLevel" -> {
                logger.info("🔧 Log level change request")
            }
            else -> {
                logger.warn("⚠️ Unknown notification: ${notification.method}")
            }
        }
    }

    private fun handleInitialize(request: JsonRpcRequest): String {
        val serverInfo = McpServerInfo(
            name = "Koog MCP Server",
            version = "0.1.0"
        )

        val capabilities = McpServerCapabilities(
            tools = McpToolsCapability(),
            resources = McpResourcesCapability(),
            prompts = McpPromptsCapability(),
            logging = McpLoggingCapability()
        )

        val response = McpInitializeResponse(
            protocolVersion = McpProtocol.VERSION,
            capabilities = capabilities,
            serverInfo = serverInfo
        )

        return createSuccessResponse(request.id, json.encodeToJsonElement(response))
    }

    private fun handleToolsList(request: JsonRpcRequest): String {
        val tools = listOf(
            mapOf(
                "name" to "weather",
                "description" to "Get weather information for a location",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "location" to mapOf(
                            "type" to "string",
                            "description" to "Location name"
                        )
                    ),
                    "required" to listOf("location")
                )
            ),
            mapOf(
                "name" to "calculator",
                "description" to "Perform mathematical calculations",
                "inputSchema" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "expression" to mapOf(
                            "type" to "string",
                            "description" to "Mathematical expression to evaluate"
                        )
                    ),
                    "required" to listOf("expression")
                )
            )
        )

        val result = mapOf("tools" to tools)
        return createSuccessResponse(request.id, json.encodeToJsonElement(result))
    }

    private fun handleToolsCall(request: JsonRpcRequest): String {
        // Tool execution would be implemented here
        val result = mapOf(
            "content" to listOf(
                mapOf(
                    "type" to "text",
                    "text" to "Tool execution result would be here"
                )
            )
        )
        return createSuccessResponse(request.id, json.encodeToJsonElement(result))
    }

    private fun handleResourcesList(request: JsonRpcRequest): String {
        val resources = listOf(
            mapOf(
                "uri" to "koog://weather",
                "name" to "Weather Data",
                "description" to "Current weather information",
                "mimeType" to "application/json"
            )
        )

        val result = mapOf("resources" to resources)
        return createSuccessResponse(request.id, json.encodeToJsonElement(result))
    }

    private fun handleResourcesRead(request: JsonRpcRequest): String {
        val content = mapOf(
            "contents" to listOf(
                mapOf(
                    "uri" to "koog://weather",
                    "mimeType" to "application/json",
                    "text" to "{\"temperature\": 25, \"condition\": \"sunny\"}"
                )
            )
        )
        return createSuccessResponse(request.id, json.encodeToJsonElement(content))
    }

    private fun handlePromptsList(request: JsonRpcRequest): String {
        val prompts = listOf(
            mapOf(
                "name" to "analyze-data",
                "description" to "Analyze and summarize data",
                "arguments" to listOf(
                    mapOf(
                        "name" to "data",
                        "description" to "Data to analyze",
                        "required" to true
                    )
                )
            )
        )

        val result = mapOf("prompts" to prompts)
        return createSuccessResponse(request.id, json.encodeToJsonElement(result))
    }

    private fun handlePromptsGet(request: JsonRpcRequest): String {
        val messages = listOf(
            mapOf(
                "role" to "user",
                "content" to mapOf(
                    "type" to "text",
                    "text" to "Please analyze the provided data and create a summary."
                )
            )
        )

        val result = mapOf("messages" to messages)
        return createSuccessResponse(request.id, json.encodeToJsonElement(result))
    }

    private fun createSuccessResponse(id: JsonElement, result: JsonElement): String {
        val response = JsonRpcResponse(
            id = id,
            result = result
        )
        return json.encodeToString(JsonRpcResponse.serializer(), response)
    }

    private fun createErrorResponse(id: JsonElement, code: Int, message: String): String {
        val response = JsonRpcResponse(
            id = id,
            error = JsonRpcError(code, message)
        )
        return json.encodeToString(JsonRpcResponse.serializer(), response)
    }
}
