package com.koog.mcp

import com.koog.mcp.server.McpServer
import com.koog.mcp.transport.StdioTransport
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

fun main(args: Array<String>) = runBlocking {
    val logger = KotlinLogging.logger {}
    
    logger.info("🚀 Starting Kotlin MCP Server...")
    
    try {
        val server = McpServer()
        val transport = StdioTransport()
        
        logger.info("📡 Using stdio transport for Koog integration")
        
        server.start(transport)
        
        logger.info("✅ MCP Server started successfully")
        
    } catch (e: Exception) {
        logger.error("❌ Failed to start MCP Server", e)
        throw e
    }
}