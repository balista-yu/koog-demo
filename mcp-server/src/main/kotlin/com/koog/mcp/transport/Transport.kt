package com.koog.mcp.transport

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow

/**
 * Transport interface for MCP communication
 */
interface Transport {
    suspend fun start()
    suspend fun stop()
    suspend fun send(message: String)
    fun receive(): Flow<String>
}

/**
 * Message container for transport
 */
data class TransportMessage(
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)