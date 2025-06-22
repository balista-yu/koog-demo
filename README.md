# Name

Koog + Spring Boot Demo

## Overview

- github repository template

## Reference
- [Koog](https://github.com/JetBrains/koog)
- [Koog ドキュメント](https://docs.koog.ai/)

## Getting Start

1. Clone the repository

```
$ git clone https://github.com/balista-yu/koog-demo.git
```

2. Make .env And set GOOGLE_API_KEY 

3. Run
```
$ task up
```

4. Call API
- シンプルチャット
  - 基本的なAIエージェントとの対話
  - エンドポイント: POST /api/koog/chat
```
curl -X POST http://localhost:8080/api/koog/chat \
   -H "Content-Type: application/json" \
   -d '{
   "message": "今日の大阪の天気を教えて",
   }'
```
- 複雑なワークフロー処理
  - 多段階のタスク処理
  - エンドポイント: POST /api/koog/workflow
```
curl -X POST http://localhost:8080/api/koog/workflow \
   -H "Content-Type: application/json" \
   -d '{
   "task": "明日の会議の準備として、天気を確認して、会議室の収容人数を計算してください"
   }'
```
