package com.koog.mcp.server

import com.koog.mcp.protocol.McpMessageHandler
import com.koog.mcp.transport.Transport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import mu.KotlinLogging

/**
 * Main MCP Server implementation
 */
class McpServer {
    private val logger = KotlinLogging.logger {}
    private val messageHandler = McpMessageHandler()
    private var isRunning = false
    private lateinit var serverJob: Job
    
    suspend fun start(transport: Transport) {
        logger.info("🚀 Starting MCP Server...")
        
        isRunning = true
        
        // Start transport
        transport.start()
        
        // Handle incoming messages
        serverJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                transport.receive().collect { message ->
                    logger.debug("📨 Processing message: $message")
                    
                    val response = messageHandler.handleMessage(message)
                    
                    if (response != null) {
                        logger.debug("📤 Sending response: $response")
                        transport.send(response)
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    logger.error("❌ Error in message processing", e)
                }
            }
        }
        
        logger.info("✅ MCP Server started and listening for messages")
        
        // Keep server running
        try {
            serverJob.join()
        } finally {
            transport.stop()
            logger.info("🛑 MCP Server stopped")
        }
    }
    
    suspend fun stop() {
        logger.info("🛑 Stopping MCP Server...")
        isRunning = false
        
        if (::serverJob.isInitialized) {
            serverJob.cancel()
        }
        
        logger.info("✅ MCP Server stopped")
    }
}