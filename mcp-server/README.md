# 🔌 Koog MCP Server

Kotlin implementation of Model Context Protocol (MCP) server for integration with Koog AI agents.

## 🎯 Overview

This MCP server provides standardized JSON-RPC 2.0 communication between Koog AI agents and external tools/data sources. It implements the official MCP specification (2025-03-26) with support for:

- **Tools**: Executable functions that agents can call
- **Resources**: Data sources that agents can read  
- **Prompts**: Template prompts for agent workflows
- **Logging**: Structured logging for debugging

## 🛠️ Technical Stack

- **Kotlin 2.1.21** + **Spring Boot 3.5.3**
- **JSON-RPC 2.0** protocol implementation
- **Coroutines** for async message handling
- **Kotlin Serialization** for JSON processing
- **SLF4J + Logback** for logging

## 🚀 Quick Start

### Docker Development (Recommended)

```bash
# From project root directory
task dev              # Start full stack (app + frontend + mcp-server)
task up-mcp           # Start only MCP server
task build-mcp        # Build MCP server container
task logs-mcp         # View MCP server logs
task test-mcp         # Test MCP functionality
```

### Local Development

```bash
# Build the server
./gradlew build

# Run with stdio transport (for Koog integration)
./gradlew run

# Or run JAR directly
java -jar build/libs/mcp-server-0.1.0-SNAPSHOT.jar
```

### Integration with Koog

```kotlin
// In your Koog agent configuration
val mcpServer = ProcessBuilder("java", "-jar", "mcp-server.jar")
    .redirectErrorStream(true)
    .start()

// Or via Docker
val mcpServer = ProcessBuilder("docker", "run", "koog-demo/mcp-server:dev")
    .redirectErrorStream(true)
    .start()
```

## 📡 Transport Methods

### 1. **stdio** (Default)
- Direct stdin/stdout communication
- Perfect for Koog agent integration  
- Zero network overhead
- Automatic process lifecycle management

### 2. **HTTP with SSE** (Future)
- Server-Sent Events for real-time communication
- OAuth 2.1 authentication
- Suitable for remote integrations

## 🔧 Available Capabilities

### Tools
- `weather` - Get weather information for locations
- `calculator` - Perform mathematical calculations
- More tools can be easily added

### Resources  
- `koog://weather` - Weather data resource
- Extensible resource system

### Prompts
- `analyze-data` - Data analysis prompt template
- Customizable prompt library

## 📋 MCP Protocol Implementation

### Message Types
```json
// Request
{
  "jsonrpc": "2.0",
  "method": "tools/list", 
  "id": 1
}

// Response
{
  "jsonrpc": "2.0",
  "result": {"tools": [...]},
  "id": 1
}

// Notification  
{
  "jsonrpc": "2.0",
  "method": "notifications/initialized"
}
```

### Initialization Flow
1. Client sends `initialize` request
2. Server responds with capabilities
3. Client sends `notifications/initialized`
4. Ready for tool/resource requests

## 🏗️ Project Structure

```
src/main/kotlin/com/koog/mcp/
├── McpServerApplication.kt     # Main entry point
├── server/
│   └── McpServer.kt           # Core server logic
├── protocol/
│   ├── JsonRpcMessage.kt      # JSON-RPC data classes
│   └── McpProtocol.kt         # Protocol handler
└── transport/
    ├── Transport.kt           # Transport interface
    └── StdioTransport.kt      # Stdio implementation
```

## 🔍 Development

### Adding New Tools

```kotlin
// In McpProtocol.kt handleToolsList()
val newTool = mapOf(
    "name" to "my-tool",
    "description" to "Description of my tool",
    "inputSchema" to mapOf(
        "type" to "object",
        "properties" to mapOf(
            "param" to mapOf(
                "type" to "string",
                "description" to "Parameter description"
            )
        ),
        "required" to listOf("param")
    )
)
```

### Testing

```bash
# Run tests
./gradlew test

# Test with stdio manually
echo '{"jsonrpc":"2.0","method":"initialize","id":1,"params":{}}' | java -jar build/libs/mcp-server.jar
```

## 📊 Monitoring

- Structured JSON logging with request/response tracking
- Configurable log levels via `application.yaml`
- Performance metrics for message processing
- Error tracking with full stack traces

## 🔗 Integration Examples

### With Koog Agents
```kotlin
val mcpServerProcess = startMcpServer()
val koogAgent = KoogAgent(mcpServerTransport = mcpServerProcess)
```

### Manual Testing
```bash
# Initialize
echo '{"jsonrpc":"2.0","method":"initialize","id":1,"params":{"protocolVersion":"2025-03-26","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' | java -jar mcp-server.jar

# List tools  
echo '{"jsonrpc":"2.0","method":"tools/list","id":2}' | java -jar mcp-server.jar
```

## 🎯 Roadmap

- [ ] HTTP/SSE transport implementation
- [ ] OAuth 2.1 authentication
- [ ] Additional tool implementations  
- [ ] Resource subscription support
- [ ] Performance optimization
- [ ] Docker containerization
- [ ] Kubernetes deployment

---

**Built for Koog AI Platform** | **MCP Specification 2025-03-26** | **Kotlin + Spring Boot**