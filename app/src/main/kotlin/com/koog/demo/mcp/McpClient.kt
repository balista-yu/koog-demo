package com.koog.demo.mcp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.atomic.AtomicLong

/**
 * MCP Client for communicating with MCP servers
 */
@Service
class McpClient {
    private val logger = KotlinLogging.logger {}
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val requestIdCounter = AtomicLong(0)
    private val _responses = MutableSharedFlow<String>()
    private val responses = _responses.asSharedFlow()
    
    private var process: Process? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var isConnected = false
    
    /**
     * Start MCP server process and initialize connection
     */
    suspend fun startServer(command: String): Boolean {
        return try {
            logger.info("🚀 Starting MCP server: $command")
            
            val processBuilder = ProcessBuilder()
            processBuilder.command("sh", "-c", command)
            
            process = processBuilder.start()
            
            writer = BufferedWriter(OutputStreamWriter(process!!.outputStream))
            reader = BufferedReader(InputStreamReader(process!!.inputStream))
            isConnected = true
            
            // Start reading responses
            startResponseReader()
            
            // Initialize the MCP protocol
            val initRequest = createInitializeRequest()
            sendRequest(initRequest)
            
            logger.info("✅ MCP server started successfully")
            true
        } catch (e: Exception) {
            logger.error("❌ Failed to start MCP server", e)
            isConnected = false
            false
        }
    }
    
    /**
     * Stop MCP server process
     */
    suspend fun stopServer() {
        logger.info("🛑 Stopping MCP server...")
        
        try {
            isConnected = false
            writer?.close()
            reader?.close()
            process?.destroyForcibly()
            process?.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
        } catch (e: Exception) {
            logger.error("Error stopping MCP server", e)
        }
        
        process = null
        writer = null
        reader = null
        
        logger.info("✅ MCP server stopped")
    }
    
    /**
     * List available tools from MCP server
     */
    suspend fun listTools(): List<McpTool> {
        if (!isConnected) {
            logger.warn("MCP server not connected")
            return emptyList()
        }
        
        return try {
            val request = createRequest("tools/list", JsonObject(emptyMap()))
            val response = sendAndWaitForResponse(request)
            
            val result = response.jsonObject["result"]?.jsonObject
            val tools = result?.get("tools")?.jsonArray
            
            tools?.map { toolElement ->
                val tool = toolElement.jsonObject
                McpTool(
                    name = tool["name"]?.jsonPrimitive?.content ?: "",
                    description = tool["description"]?.jsonPrimitive?.content ?: "",
                    inputSchema = tool["inputSchema"]?.jsonObject
                )
            } ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error parsing tools list", e)
            isConnected = false
            emptyList()
        }
    }
    
    /**
     * Call a tool on the MCP server
     */
    suspend fun callTool(name: String, arguments: JsonObject): String {
        if (!isConnected) {
            return "MCP server not connected"
        }
        
        return try {
            val params = JsonObject(mapOf(
                "name" to JsonPrimitive(name),
                "arguments" to arguments
            ))
            
            val request = createRequest("tools/call", params)
            val response = sendAndWaitForResponse(request)
            
            val result = response.jsonObject["result"]?.jsonObject
            val content = result?.get("content")?.jsonArray?.firstOrNull()?.jsonObject
            content?.get("text")?.jsonPrimitive?.content ?: "No result"
        } catch (e: Exception) {
            logger.error("Error calling tool", e)
            isConnected = false
            "Error: ${e.message}"
        }
    }
    
    /**
     * List available resources from MCP server
     */
    suspend fun listResources(): List<McpResource> {
        if (!isConnected) {
            logger.warn("MCP server not connected")
            return emptyList()
        }
        
        return try {
            val request = createRequest("resources/list", JsonObject(emptyMap()))
            val response = sendAndWaitForResponse(request)
            
            val result = response.jsonObject["result"]?.jsonObject
            val resources = result?.get("resources")?.jsonArray
            
            resources?.map { resourceElement ->
                val resource = resourceElement.jsonObject
                McpResource(
                    uri = resource["uri"]?.jsonPrimitive?.content ?: "",
                    name = resource["name"]?.jsonPrimitive?.content ?: "",
                    description = resource["description"]?.jsonPrimitive?.content ?: "",
                    mimeType = resource["mimeType"]?.jsonPrimitive?.content
                )
            } ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error parsing resources list", e)
            isConnected = false
            emptyList()
        }
    }
    
    /**
     * Read a resource from the MCP server
     */
    suspend fun readResource(uri: String): String {
        if (!isConnected) {
            return "MCP server not connected"
        }
        
        return try {
            val params = JsonObject(mapOf(
                "uri" to JsonPrimitive(uri)
            ))
            
            val request = createRequest("resources/read", params)
            val response = sendAndWaitForResponse(request)
            
            val result = response.jsonObject["result"]?.jsonObject
            val contents = result?.get("contents")?.jsonArray?.firstOrNull()?.jsonObject
            contents?.get("text")?.jsonPrimitive?.content ?: "No content"
        } catch (e: Exception) {
            logger.error("Error reading resource", e)
            isConnected = false
            "Error: ${e.message}"
        }
    }
    
    private fun createInitializeRequest(): String {
        val params = JsonObject(mapOf(
            "protocolVersion" to JsonPrimitive("2025-03-26"),
            "capabilities" to JsonObject(mapOf(
                "tools" to JsonObject(emptyMap()),
                "resources" to JsonObject(emptyMap())
            )),
            "clientInfo" to JsonObject(mapOf(
                "name" to JsonPrimitive("Koog Demo Client"),
                "version" to JsonPrimitive("0.1.0")
            ))
        ))
        
        return createRequest("initialize", params)
    }
    
    private fun createRequest(method: String, params: JsonObject): String {
        val id = requestIdCounter.incrementAndGet()
        val request = JsonObject(mapOf(
            "jsonrpc" to JsonPrimitive("2.0"),
            "id" to JsonPrimitive(id),
            "method" to JsonPrimitive(method),
            "params" to params
        ))
        
        return json.encodeToString(JsonObject.serializer(), request)
    }
    
    private suspend fun sendRequest(request: String) {
        try {
            if (!isConnected || writer == null) {
                logger.warn("Cannot send request: MCP server not connected")
                isConnected = false
                throw IllegalStateException("MCP server not connected")
            }
            
            logger.debug("📤 Sending: $request")
            writer?.write(request)
            writer?.newLine()
            writer?.flush()
        } catch (e: Exception) {
            logger.error("Error sending request", e)
            isConnected = false
            throw e
        }
    }
    
    private suspend fun sendAndWaitForResponse(request: String): JsonElement {
        sendRequest(request)
        
        // Wait for response (simplified - in production, match by ID)
        return withTimeout(30000) {
            var response: JsonElement? = null
            responses.collect { responseStr ->
                try {
                    response = json.parseToJsonElement(responseStr)
                    return@collect
                } catch (e: Exception) {
                    logger.debug("Skipping non-JSON response: $responseStr")
                }
            }
            response ?: JsonNull
        }
    }
    
    private fun startResponseReader() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                reader?.use { reader ->
                    reader.lineSequence().forEach { line ->
                        if (line.isNotBlank()) {
                            logger.debug("📨 Received: $line")
                            _responses.tryEmit(line)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Error reading responses", e)
            }
        }
    }
}

/**
 * Data classes for MCP entities
 */
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject?
)

data class McpResource(
    val uri: String,
    val name: String,
    val description: String,
    val mimeType: String?
)