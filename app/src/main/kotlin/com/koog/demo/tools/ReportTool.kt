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

object ReportTool : SimpleTool<ReportTool.Args>() {
    @Serializable
    data class Args(
        val title: String,
        val sections: String,
        val format: String = "text"
    ) : Tool.Args

    override val argsSerializer = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "generate_report",
        description = "構造化されたレポートを生成します",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "title",
                description = "レポートのタイトル",
                type = ToolParameterType.String,
            ),
            ToolParameterDescriptor(
                name = "sections",
                description = "セクション内容（JSON形式: {\"section1\": \"content1\", \"section2\": \"content2\"}）",
                type = ToolParameterType.String,
            )
        ),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "format",
                description = "出力フォーマット（text, markdown, html）",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return try {
            val reportId = "RPT-${Random.nextInt(1000, 9999)}"
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            when (args.format.lowercase()) {
                "markdown" -> generateMarkdownReport(args.title, args.sections, reportId, timestamp)
                "html" -> generateHtmlReport(args.title, args.sections, reportId, timestamp)
                else -> generateTextReport(args.title, args.sections, reportId, timestamp)
            }
        } catch (e: Exception) {
            "レポート生成エラー: ${e.message}"
        }
    }

    private fun generateTextReport(title: String, sections: String, reportId: String, timestamp: String): String {
        return """
        ═══════════════════════════════════════
        📊 $title
        ═══════════════════════════════════════
        レポートID: $reportId
        生成日時: $timestamp

        $sections

        ───────────────────────────────────────
        レポート生成完了
        """.trimIndent()
    }

    private fun generateMarkdownReport(title: String, sections: String, reportId: String, timestamp: String): String {
        return """
        # 📊 $title

        **レポートID:** $reportId
        **生成日時:** $timestamp

        ## 📋 詳細内容

        $sections

        ---
        *レポート生成完了*
        """.trimIndent()
    }

    private fun generateHtmlReport(title: String, sections: String, reportId: String, timestamp: String): String {
        return """
        <html>
        <head><title>$title</title></head>
        <body>
        <h1>📊 $title</h1>
        <p><strong>レポートID:</strong> $reportId</p>
        <p><strong>生成日時:</strong> $timestamp</p>
        <div>$sections</div>
        <hr>
        <p><em>レポート生成完了</em></p>
        </body>
        </html>
        """.trimIndent()
    }
}