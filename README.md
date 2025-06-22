# 🤖 Koog + Spring Boot Demo

KoogエージェントとSpring Bootを組み合わせたAI業務自動化デモ

## ✨ 機能

- **AIエージェント**: 自然言語での業務指示
- **天気情報**: リアルタイム天気データ取得
- **データ分析**: JSON/CSV形式のデータ統計・集計
- **レポート生成**: 自動レポート作成
- **日時計算**: スケジュール管理・期間計算
- **通知システム**: アラート・ログ出力

## 🚀 クイックスタート

### 1. セットアップ
```bash
git clone https://github.com/balista-yu/koog-demo.git
cd koog-demo
```

### 2. 環境変数設定
`.env`ファイルを作成：
```bash
GOOGLE_API_KEY=your_google_api_key
OPENWEATHER_API_KEY=your_weather_api_key
```

### 3. 起動
```bash
task up
```

## 📡 API使用例

### シンプルチャット
```bash
curl -X POST http://localhost:8080/api/koog/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is the weather in Osaka?"}'
```

### 複雑ワークフロー
```bash
curl -X POST http://localhost:8080/api/koog/workflow \
  -H "Content-Type: application/json" \
  -d '{"task": "プロジェクト開始日を2025-01-15、完了予定日を2025-03-30として、残り日数を計算し、進捗アラートを設定して、プロジェクト管理レポートを作成してください"}'
```

## 🛠️ 技術構成

- Spring Boot 3.5.3 + Kotlin
- Koog Agents 0.2.1
- Google Gemini 2.0 Flash
- Docker + Docker Compose

## 📚 参考

- [Koog](https://github.com/JetBrains/koog)
- [Koog ドキュメント](https://docs.koog.ai/)
