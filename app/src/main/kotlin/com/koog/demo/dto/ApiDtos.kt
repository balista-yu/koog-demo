package com.koog.demo.dto

data class ChatRequest(
    val message: String,
    val context: String? = null
)

data class ChatResponse(
    val response: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "success"
)

data class WorkflowRequest(
    val task: String
)

data class ErrorResponse(
    val message: String,
    val code: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, Any>? = null
)