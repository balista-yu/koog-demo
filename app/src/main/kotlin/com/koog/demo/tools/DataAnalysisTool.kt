package com.koog.demo.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable

object DataAnalysisTool : SimpleTool<DataAnalysisTool.Args>() {
    @Serializable
    data class Args(
        val data: String,
        val operation: String = "summary",
        val format: String = "json"
    ) : Tool.Args

    override val argsSerializer = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "analyze_data",
        description = "データの分析と集計を行います（JSON/CSV形式のデータを解析）",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "data",
                description = "分析対象のデータ（JSON配列またはCSV形式）",
                type = ToolParameterType.String,
            )
        ),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "operation",
                description = "実行する分析操作（summary, count, average, max, min）",
                type = ToolParameterType.String,
            ),
            ToolParameterDescriptor(
                name = "format",
                description = "データフォーマット（json, csv）",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return try {
            when (args.format.lowercase()) {
                "json" -> analyzeJsonData(args.data, args.operation)
                "csv" -> analyzeCsvData(args.data, args.operation)
                else -> "サポートされていないフォーマットです: ${args.format}"
            }
        } catch (e: Exception) {
            "データ分析エラー: ${e.message}"
        }
    }

    private fun analyzeJsonData(data: String, operation: String): String {
        val numbers = data.replace("[", "").replace("]", "")
            .split(",").mapNotNull { it.trim().toDoubleOrNull() }

        if (numbers.isEmpty()) return "有効な数値データが見つかりません"

        return when (operation.lowercase()) {
            "summary" -> "データ件数: ${numbers.size}, 合計: ${numbers.sum()}, 平均: ${"%.2f".format(numbers.average())}, 最大: ${numbers.maxOrNull()}, 最小: ${numbers.minOrNull()}"
            "count" -> "データ件数: ${numbers.size}"
            "average" -> "平均値: ${"%.2f".format(numbers.average())}"
            "max" -> "最大値: ${numbers.maxOrNull()}"
            "min" -> "最小値: ${numbers.minOrNull()}"
            "sum" -> "合計値: ${numbers.sum()}"
            else -> "サポートされていない操作です: $operation"
        }
    }

    private fun analyzeCsvData(data: String, operation: String): String {
        val lines = data.trim().split("\n")
        if (lines.size < 2) return "CSVデータが不完全です（ヘッダーとデータが必要）"

        val headers = lines[0].split(",").map { it.trim() }
        val dataRows = lines.drop(1)

        return "CSV分析結果:\n" +
               "ヘッダー: ${headers.joinToString(", ")}\n" +
               "データ行数: ${dataRows.size}\n" +
               "列数: ${headers.size}"
    }
}