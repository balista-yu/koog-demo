package com.koog.demo.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import kotlinx.serialization.Serializable

object CalculatorTool : SimpleTool<CalculatorTool.Args>() {
    @Serializable
    data class Args(
        val expression: String,
        val operation: String = "basic"
    ) : Tool.Args

    override val argsSerializer = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "calculator",
        description = "計算を実行します",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "expression",
                description = "実行する計算式",
                type = ToolParameterType.String,
            )
        ),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "operation",
                description = "計算のタイプ",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return try {
            when {
                "+" in args.expression -> {
                    val parts = args.expression.split("+")
                    val result = parts.sumOf { it.trim().toDouble() }
                    "計算結果: $result"
                }
                "*" in args.expression -> {
                    val parts = args.expression.split("*")
                    val result = parts.fold(1.0) { acc, part -> acc * part.trim().toDouble() }
                    "計算結果: $result"
                }
                "-" in args.expression -> {
                    val parts = args.expression.split("-")
                    val result = parts.first().trim().toDouble() - parts.drop(1).sumOf { it.trim().toDouble() }
                    "計算結果: $result"
                }
                "/" in args.expression -> {
                    val parts = args.expression.split("/")
                    val result = parts.fold(parts.first().trim().toDouble()) { acc, part ->
                        if (part == parts.first()) acc else acc / part.trim().toDouble()
                    }
                    "計算結果: $result"
                }
                else -> "対応していない計算式です: ${args.expression}"
            }
        } catch (e: Exception) {
            "計算エラー: ${e.message}"
        }
    }
}