package com.koog.demo.tools

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import com.koog.demo.service.WeatherService
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer

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