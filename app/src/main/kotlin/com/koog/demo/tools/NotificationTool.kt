package com.koog.demo.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object NotificationTool : SimpleTool<NotificationTool.Args>() {
    @Serializable
    data class Args(
        val message: String,
        val level: String = "info",
        val category: String = "general"
    ) : Tool.Args

    override val argsSerializer = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "send_notification",
        description = "通知やアラートを生成します",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "message",
                description = "通知メッセージ",
                type = ToolParameterType.String,
            )
        ),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "level",
                description = "通知レベル（info, warning, error, success）",
                type = ToolParameterType.String,
            ),
            ToolParameterDescriptor(
                name = "category",
                description = "通知カテゴリ",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val icon = when (args.level.lowercase()) {
            "error" -> "❌"
            "warning" -> "⚠️"
            "success" -> "✅"
            else -> "ℹ️"
        }

        val notification = """
        $icon 【${args.level.uppercase()}】${args.category}
        時刻: $timestamp
        メッセージ: ${args.message}

        通知ID: NOTIF-${Random.nextInt(1000, 9999)}
        """.trimIndent()

        println("NOTIFICATION: $notification")

        return "通知を送信しました:\n$notification"
    }
}