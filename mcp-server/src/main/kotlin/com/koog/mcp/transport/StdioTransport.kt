package com.koog.mcp.transport

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter

/**
 * Standard I/O Transport for MCP
 * Used for direct communication with Koog agents
 */
class StdioTransport : Transport {
    private val logger = KotlinLogging.logger {}
    private val messageChannel = Channel<String>(Channel.UNLIMITED)
    private var isRunning = false
    
    private lateinit var reader: BufferedReader
    private lateinit var writer: PrintWriter
    private lateinit var readerJob: Job
    
    override suspend fun start() {
        logger.info("🚀 Starting stdio transport...")
        
        reader = BufferedReader(InputStreamReader(System.`in`))
        writer = PrintWriter(OutputStreamWriter(System.out), true)
        
        isRunning = true
        
        // Start reading from stdin in a separate coroutine
        readerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (isRunning) {
                    val line = reader.readLine()
                    if (line != null) {
                        logger.debug("📨 Read from stdin: $line")
                        messageChannel.send(line)
                    } else {
                        logger.info("📡 EOF reached, stopping transport")
                        break
                    }
                }
            } catch (e: Exception) {
                if (isRunning) {
                    logger.error("❌ Error reading from stdin", e)
                }
            }
        }
        
        logger.info("✅ Stdio transport started")
    }
    
    override suspend fun stop() {
        logger.info("🛑 Stopping stdio transport...")
        
        isRunning = false
        
        try {
            readerJob.cancel()
            messageChannel.close()
            reader.close()
            writer.close()
        } catch (e: Exception) {
            logger.error("❌ Error stopping transport", e)
        }
        
        logger.info("✅ Stdio transport stopped")
    }
    
    override suspend fun send(message: String) {
        try {
            logger.debug("📤 Sending to stdout: $message")
            writer.println(message)
            writer.flush()
        } catch (e: Exception) {
            logger.error("❌ Error sending message", e)
            throw e
        }
    }
    
    override fun receive(): Flow<String> = flow {
        try {
            while (isRunning) {
                val message = messageChannel.receive()
                emit(message)
            }
        } catch (e: Exception) {
            if (isRunning) {
                logger.error("❌ Error in receive flow", e)
                throw e
            }
        }
    }
}