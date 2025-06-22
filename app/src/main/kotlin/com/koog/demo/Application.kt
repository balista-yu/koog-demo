package com.koog.demo

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@ConfigurationPropertiesScan
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Component
data class KoogProperties(
    val googleApiKey: String = System.getenv("GOOGLE_API_KEY")
)

data class ChatRequest(
    val message: String,
    val context: String? = null,
)

data class ChatResponse(
    val response: String?,
    val timestamp: Long = System.currentTimeMillis()
)

object WeatherTool : SimpleTool<WeatherTool.Args>() {
    @Serializable
    data class Args(
        val location: String = "大阪"
    ) : Tool.Args

    override val argsSerializer: KSerializer<Args> = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "get_weather",
        description = "天気情報を取得します",
        requiredParameters = emptyList(),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "location",
                description = "天気を取得する地域名",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return "今日の${args.location}の天気は晴れ、気温は25度です。"
    }
}

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

@Service
class KoogAgentService(private val koogProperties: KoogProperties) {
    private val toolRegistry = ToolRegistry {
        tool(WeatherTool)
        tool(CalculatorTool)
    }

    suspend fun chatWithAgent(request: ChatRequest): String? {
        return try {
            val agent = AIAgent(
                executor = simpleGoogleAIExecutor(koogProperties.googleApiKey),
                toolRegistry = toolRegistry,
                systemPrompt = """
                    あなたは親切なAIアシスタントです。
                    ユーザーの質問に丁寧に答えてください。
                    必要に応じて利用可能なツールを使用してください。

                    利用可能なツール:
                    - get_weather: 天気情報を取得
                    - calculator: 簡単な計算を実行
                """.trimIndent(),
                llmModel = GoogleModels.Gemini2_0Flash001
            )

            val response = agent.runAndGetResult(request.message)
            response
        } catch (e: Exception) {
            "申し訳ございません。処理中にエラーが発生しました: ${e.message}"
        }
    }

    suspend fun processComplexTask(task: String): String? {
        return try {
            val complexAgent = AIAgent(
                executor = simpleGoogleAIExecutor(koogProperties.googleApiKey),
                toolRegistry = toolRegistry,
                systemPrompt = """
                    あなたは複雑なタスクを段階的に処理できるAIエージェントです。
                    以下のステップで処理してください：

                    1. **タスク分析**: まず与えられたタスクを詳しく分析し、何をする必要があるかを明確にする
                    2. **必要なツール特定**: タスクを完了するために必要なツールを特定する
                    3. **段階的実行**: 特定したツールを適切な順序で使用してタスクを実行する
                    4. **結果統合**: 各ステップの結果を統合し、わかりやすい最終回答を提供する

                    利用可能なツール:
                    - get_weather: 天気情報を取得
                    - calculator: 数値計算を実行

                    各ステップを明確に示しながら、ユーザーにとって有用な回答を提供してください。
                """.trimIndent(),
                llmModel = GoogleModels.Gemini2_0Flash001
            )

            val response = complexAgent.runAndGetResult(task)
            response
        } catch (e: Exception) {
            "ワークフロー処理中にエラーが発生しました: ${e.message}"
        }
    }
}

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
