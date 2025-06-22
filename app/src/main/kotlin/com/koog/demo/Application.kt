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
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.http.codec.json.KotlinSerializationJsonEncoder
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(WeatherProperties::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Component
data class KoogProperties(
    val googleApiKey: String = System.getenv("GOOGLE_API_KEY")
)

@ConfigurationProperties(prefix = "weather.api")
data class WeatherProperties(
    val key: String = "",
    val url: String = "https://api.openweathermap.org/data/2.5/weather"
)

@Serializable
data class WeatherResponse(
    val name: String? = null,
    val main: Main? = null,
    val weather: List<Weather>? = null,
    val cod: Int? = null,
    val message: String? = null
) {
    @Serializable
    data class Main(
        val temp: Double,
        val feels_like: Double,
        val humidity: Int
    )

    @Serializable
    data class Weather(
        val main: String,
        val description: String
    )
}

@Configuration
class WebClientConfig {
    @Bean
    fun webClient(): WebClient {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().kotlinSerializationJsonDecoder(KotlinSerializationJsonDecoder(json))
                configurer.defaultCodecs().kotlinSerializationJsonEncoder(KotlinSerializationJsonEncoder(json))
            }
            .build()

        return WebClient.builder()
            .exchangeStrategies(strategies)
            .build()
    }
}

@Service
class WeatherService(
    private val weatherProperties: WeatherProperties,
    private val webClient: WebClient
) {
    suspend fun getWeather(location: String): String {
        return try {
            println("WeatherService: API key = '${weatherProperties.key}' (length: ${weatherProperties.key.length})")
            if (weatherProperties.key.isEmpty()) {
                return "天気APIキーが設定されていません。"
            }

            val response = webClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .scheme("https")
                        .host("api.openweathermap.org")
                        .path("/data/2.5/weather")
                        .queryParam("q", location)
                        .queryParam("appid", weatherProperties.key)
                        .queryParam("units", "metric")
                        .queryParam("lang", "ja")
                        .build()
                }
                .retrieve()
                .awaitBody<WeatherResponse>()

            println("WeatherService: API call successful for location: $location")

            println("WeatherService: Response received: $response")

            if (response.cod != null && response.cod != 200) {
                return "OpenWeatherMap APIエラー: ${response.message ?: "不明なエラー"}"
            }

            if (response.name == null || response.main == null || response.weather == null) {
                return "天気データが不完全です。"
            }

            "今日の${response.name}の天気は${response.weather.firstOrNull()?.description ?: "不明"}、" +
            "気温は${response.main.temp.toInt()}度、体感温度は${response.main.feels_like.toInt()}度、" +
            "湿度は${response.main.humidity}%です。"
        } catch (e: Exception) {
            println("WeatherService: Error occurred: ${e.javaClass.simpleName}: ${e.message}")
            e.printStackTrace()
            "天気情報の取得に失敗しました: ${e.message}。今日の${location}の天気は晴れ、気温は25度です。（固定値）"
        }
    }
}

data class ChatRequest(
    val message: String,
    val context: String? = null,
)

data class ChatResponse(
    val response: String?,
    val timestamp: Long = System.currentTimeMillis()
)

class WeatherTool(private val weatherService: WeatherService) : SimpleTool<WeatherTool.Args>() {
    @Serializable
    data class Args(
        val location: String = "大阪"
    ) : Tool.Args

    override val argsSerializer: KSerializer<Args> = Args.serializer()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = "get_weather",
        description = "指定した地域の現在の天気情報を取得します",
        requiredParameters = emptyList(),
        optionalParameters = listOf(
            ToolParameterDescriptor(
                name = "location",
                description = "天気を取得する地域名（都市名）",
                type = ToolParameterType.String,
            )
        )
    )

    override suspend fun doExecute(args: Args): String {
        return weatherService.getWeather(args.location)
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
        // 簡単なJSON配列の数値データ分析
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

        // ログにも出力
        println("NOTIFICATION: $notification")

        return "通知を送信しました:\n$notification"
    }
}

@Service
class KoogAgentService(
    private val koogProperties: KoogProperties,
    private val weatherService: WeatherService
) {
    private val toolRegistry = ToolRegistry {
        tool(WeatherTool(weatherService))
        tool(CalculatorTool)
        tool(DataAnalysisTool)
        tool(DateTimeTool)
        tool(ReportTool)
        tool(NotificationTool)
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
                    - calculator: 数値計算を実行
                    - analyze_data: データ分析と集計
                    - datetime_helper: 日時の計算と操作
                    - generate_report: レポート生成
                    - send_notification: 通知・アラート送信
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
                    あなたは高度な業務自動化とデータ分析を行うAIエージェントです。
                    複雑な業務タスクを段階的に処理し、実用的な成果を提供します。

                    🎯 **ワークフロー処理手順**:
                    1. **要求分析**: タスクの詳細を分析し、成果物を明確にする
                    2. **実行計画**: 必要なツールと処理順序を計画する
                    3. **段階的実行**: 計画に従ってツールを組み合わせて実行する
                    4. **品質確認**: 結果の検証と改善点の特定
                    5. **成果報告**: 構造化された最終レポートを提供する

                    🛠️ **利用可能なツール**:
                    - **get_weather**: リアルタイム天気情報の取得
                    - **calculator**: 高度な数値計算・統計処理
                    - **analyze_data**: データ分析・統計・集計処理
                    - **datetime_helper**: 日時計算・スケジュール管理
                    - **generate_report**: 構造化レポート生成（Text/Markdown/HTML）
                    - **send_notification**: 通知・アラート・ログ出力

                    💼 **対応可能な業務例**:
                    - データ分析レポートの作成
                    - 売上・業績データの集計分析
                    - スケジュール管理と期間計算
                    - 業務通知とアラート設定
                    - 複数データソースの統合分析
                    - 定期レポートの自動生成

                    📋 **実行方針**:
                    - 各ステップの進行状況を明確に報告
                    - エラー発生時は代替手段を提案
                    - 実用的で行動可能な結果を提供
                    - 必要に応じて複数ツールを組み合わせて使用
                    - 最終成果物は必ずレポート形式で整理

                    効率的で実用的な業務支援を提供します。
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
