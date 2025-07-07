#!/bin/bash

# Koog MCP Server startup script
echo "🚀 Starting Koog MCP Server..."

# Wait for dependencies (if any)
sleep 2

# Start the MCP server
echo "📡 MCP Server listening on stdio transport"
echo "🔗 Ready for Koog agent integration"

exec java -jar app.jar