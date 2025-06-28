# 🎨 Koog Demo Dashboard

Koog AI エージェントシステムのフロントエンドダッシュボード

## ✨ 機能

- **💬 AIチャット** - リアルタイムチャットインターフェース
- **🔧 ワークフロー実行** - 複雑なタスクの段階的実行
- **📊 ツール統合** - 天気、計算、データ分析、レポート生成
- **📱 レスポンシブデザイン** - モバイル対応UI

## 🛠️ 技術スタック

- **React 19** + **TypeScript**
- **Vite** - 高速ビルドツール
- **Tailwind CSS** - ユーティリティファーストCSS
- **React Query** - サーバー状態管理
- **Axios** - HTTP クライアント
- **Lucide React** - アイコン
- **Vitest** - テストフレームワーク

## 🚀 セットアップ

### 前提条件
- Node.js 22.4+
- npm 10+
- バックエンドAPIサーバー（localhost:8080）が起動済み

### インストール
```bash
# 依存関係のインストール
npm install

# 開発サーバー起動
npm run dev
# または
./dev.sh

# アプリケーションアクセス
# http://localhost:3000
```

### API 設定
```bash
# .env.development (自動作成済み)
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=Koog Demo Dashboard
VITE_DEBUG=true
```

## 📝 利用可能なスクリプト

```bash
npm run dev          # 開発サーバー起動
npm run build        # プロダクションビルド
npm run preview      # ビルド結果のプレビュー
npm run test         # テスト実行
npm run test:watch   # テスト監視モード
npm run lint         # ESLint実行
```

## 🏗️ プロジェクト構造

```
src/
├── components/           # React コンポーネント
│   ├── ChatInterface.tsx     # チャット画面
│   ├── WorkflowInterface.tsx # ワークフロー画面
│   └── __tests__/           # コンポーネントテスト
├── hooks/               # カスタムフック
│   └── useChat.ts          # チャットAPI フック
├── services/            # API サービス
│   └── api.ts              # HTTP クライアント設定
├── types/               # TypeScript 型定義
│   └── api.ts              # API 型定義
├── utils/               # ユーティリティ関数
└── test/                # テスト設定
    └── setup.ts            # テストセットアップ
```

## 🔌 API 統合

### チャット API
```typescript
POST /api/chat
{
  "message": "今日の天気は？",
  "context": "大阪"
}
```

### ワークフロー API  
```typescript
POST /api/workflow
{
  "task": "今日の東京の天気を調べて、温度に基づいて服装のアドバイスをください"
}
```

## 🧪 テスト

```bash
# 全テスト実行
npm run test

# 監視モード
npm run test:watch

# 特定ファイルのテスト
npm run test ChatInterface
```

## 📱 UI コンポーネント

### ChatInterface
- リアルタイムメッセージング
- ローディング状態表示
- エラーハンドリング
- 自動スクロール

### WorkflowInterface  
- 複雑タスクの実行
- サンプルタスク提供
- 結果履歴表示
- ステータス管理

## 🚦 開発ワークフロー

### Docker開発環境 (推奨)
1. **フルスタック起動**: `task dev` (ルートディレクトリ)
2. **ブラウザアクセス**: http://localhost:3000
3. **API接続**: コンテナ間通信で自動設定

### ローカル開発環境
1. **バックエンド起動**: `task up` (ルートディレクトリ)
2. **フロントエンド起動**: `./dev.sh` または `npm run dev`
3. **ブラウザアクセス**: http://localhost:3000
4. **API プロキシ**: `/api/*` → `http://localhost:8080/*`

## 🔧 トラブルシューティング

### API接続エラー
- バックエンドサーバーが起動している確認
- `curl http://localhost:8080/api/chat` でAPIテスト
- Dockerの場合は `task logs-app` でバックエンドログ確認

### ビルドエラー
- `npm ci` で依存関係を再インストール
- Node.js バージョン確認 (22.4+)

### テストエラー
- `npm run test -- --reporter=verbose` で詳細確認

## 📈 今後の改善予定

- [ ] WebSocket によるリアルタイム通信
- [ ] チャット履歴の永続化
- [ ] ダークモード対応
- [ ] 国際化 (i18n) サポート
- [ ] PWA 対応
- [ ] API レスポンス キャッシュ最適化
