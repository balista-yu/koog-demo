#!/bin/bash

# 開発サーバー起動スクリプト
echo "🚀 Koog UI 開発サーバーを起動中..."

# API サーバーの確認
echo "📡 API サーバー(localhost:8080)の確認中..."
if curl -s http://localhost:8080/api/koog/chat > /dev/null 2>&1; then
    echo "✅ API サーバーが起動済みです"
else
    echo "⚠️  API サーバーが起動していません"
    echo "   バックエンドサーバーを起動してください: task up"
fi

# フロントエンド開発サーバー起動
echo "🎨 フロントエンド開発サーバーを起動中..."
echo "   URL: http://localhost:3000"
echo "   プロキシ: /api -> http://localhost:8080"
echo ""

npm run dev