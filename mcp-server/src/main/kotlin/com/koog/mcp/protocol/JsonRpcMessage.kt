package com.koog.mcp.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * JSON-RPC 2.0 Message Types for MCP
 */
@Serializable
sealed class JsonRpcMessage {
    abstract val jsonrpc: String
}

@Serializable
data class JsonRpcRequest(
    override val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonElement? = null,
    val id: JsonElement
) : JsonRpcMessage()

@Serializable
data class JsonRpcResponse(
    override val jsonrpc: String = "2.0",
    val result: JsonElement? = null,
    val error: JsonRpcError? = null,
    val id: JsonElement
) : JsonRpcMessage()

@Serializable
data class JsonRpcNotification(
    override val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonElement? = null
) : JsonRpcMessage()

@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

/**
 * MCP-specific message types
 */
@Serializable
data class McpInitializeRequest(
    val protocolVersion: String,
    val capabilities: McpClientCapabilities,
    val clientInfo: McpClientInfo
)

@Serializable
data class McpInitializeResponse(
    val protocolVersion: String,
    val capabilities: McpServerCapabilities,
    val serverInfo: McpServerInfo
)

@Serializable
data class McpClientCapabilities(
    val roots: McpRootsCapability? = null,
    val sampling: McpSamplingCapability? = null
)

@Serializable
data class McpServerCapabilities(
    val logging: McpLoggingCapability? = null,
    val prompts: McpPromptsCapability? = null,
    val resources: McpResourcesCapability? = null,
    val tools: McpToolsCapability? = null
)

@Serializable
data class McpClientInfo(
    val name: String,
    val version: String
)

@Serializable
data class McpServerInfo(
    val name: String,
    val version: String
)

@Serializable
data class McpRootsCapability(
    val listChanged: Boolean? = null
)

@Serializable
data class McpSamplingCapability(
    val enabled: Boolean = false
)

@Serializable
data class McpLoggingCapability(
    val enabled: Boolean = true
)

@Serializable
data class McpPromptsCapability(
    val listChanged: Boolean? = null
)

@Serializable
data class McpResourcesCapability(
    val subscribe: Boolean? = null,
    val listChanged: Boolean? = null
)

@Serializable
data class McpToolsCapability(
    val listChanged: Boolean? = null
)