package com.koog.demo.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeTool : SimpleTool<DateTimeTool.Args>() {
    @Serializable
    data class Args(
        val operation: String = "current",
        val date1: String = "",
        val date2: String = "",
        val format: String = "yyyy-MM-dd HH:mm:ss"
    ) : Tool.Args

    override val argsSerializer = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "datetime_helper",
        description = "日時の計算、フォーマット、期間計算を行います",
        requiredParameters = emptyList(),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "operation",
                description = "実行する操作（current, diff, add_days, format）",
                type = ToolParameterType.String,
            ),
            ToolParameterDescriptor(
                name = "date1",
                description = "基準日時（yyyy-MM-dd HH:mm:ss形式）",
                type = ToolParameterType.String,
            ),
            ToolParameterDescriptor(
                name = "date2",
                description = "比較日時（yyyy-MM-dd HH:mm:ss形式）",
                type = ToolParameterType.String,
            ),
            ToolParameterDescriptor(
                name = "format",
                description = "出力フォーマット",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return try {
            when (args.operation.lowercase()) {
                "current" -> "現在時刻: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern(args.format))}"
                "diff" -> calculateDateDifference(args.date1, args.date2)
                "add_days" -> addDaysToDate(args.date1, args.date2.toIntOrNull() ?: 0, args.format)
                "format" -> formatDate(args.date1, args.format)
                else -> "サポートされていない操作です: ${args.operation}"
            }
        } catch (e: Exception) {
            "日時処理エラー: ${e.message}"
        }
    }

    private fun calculateDateDifference(date1: String, date2: String): String {
        val dt1 = LocalDateTime.parse(date1.replace(" ", "T"))
        val dt2 = LocalDateTime.parse(date2.replace(" ", "T"))
        val days = ChronoUnit.DAYS.between(dt1, dt2)
        val hours = ChronoUnit.HOURS.between(dt1, dt2) % 24
        return "期間差: ${days}日 ${hours}時間"
    }

    private fun addDaysToDate(date: String, days: Int, format: String): String {
        val dt = LocalDateTime.parse(date.replace(" ", "T"))
        val newDate = dt.plusDays(days.toLong())
        return "計算結果: ${newDate.format(DateTimeFormatter.ofPattern(format))}"
    }

    private fun formatDate(date: String, format: String): String {
        val dt = LocalDateTime.parse(date.replace(" ", "T"))
        return "フォーマット結果: ${dt.format(DateTimeFormatter.ofPattern(format))}"
    }
}