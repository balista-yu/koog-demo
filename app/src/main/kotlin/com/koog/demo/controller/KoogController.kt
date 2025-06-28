package com.koog.demo.controller

import com.koog.demo.dto.ChatRequest
import com.koog.demo.dto.ChatResponse
import com.koog.demo.service.KoogAgentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/koog")
class KoogController(private val koogAgentService: KoogAgentService) {

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
}