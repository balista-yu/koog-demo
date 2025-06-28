package com.koog.demo.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.koog.demo.config.KoogProperties
import com.koog.demo.dto.ChatRequest
import com.koog.demo.tools.*
import org.springframework.stereotype.Service

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