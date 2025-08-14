package com.example.spring_ai_azure_openai_streaming_issue

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.RepetitionInfo
import org.springframework.ai.azure.openai.AzureOpenAiChatModel
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ToolContext
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.tool.function.FunctionToolCallback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.function.BiFunction
import kotlin.jvm.java
import kotlin.test.assertEquals

@SpringBootTest(
    classes = [SpringAiAzureOpenaiStreamingIssueApplication::class],
)
class SpringAiAzureOpenaiStreamingIssueApplicationTests(
    @Autowired chatModel: AzureOpenAiChatModel,
) {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            WireMockServer(
                options()
                    .httpsPort(8443)
                    .keystorePath("certs/keystore.jks")
                    .keystorePassword("password")
                    .usingFilesUnderDirectory("src/test/resources/wiremock")
            ).start()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            WireMock.shutdownServer()
        }
    }

    private val chatClient = ChatClient.builder(chatModel).build()

    private val weatherTool = FunctionToolCallback
        .builder(
            "getWeather",
            BiFunction<Unit, ToolContext, Unit> { input, toolContext ->
                "Sunny, 25°C"
            },
        ).description("Get the current weather")
        .inputType(Unit::class.java)
        .build()

    private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val expectedResult = (0..<1000).joinToString("\n") { i ->
        chars[(i % chars.size)].toString()
    }

    @RepeatedTest(20000, failureThreshold = 1)
    fun `simple response - order should not be messed up`(repetitionInfo: RepetitionInfo) {
        println("Simple - Repetition ${repetitionInfo.currentRepetition} of ${repetitionInfo.totalRepetitions}")
        if (repetitionInfo.currentRepetition % 50 == 0) {
            WireMock.reset()
            System.gc()
        }

        val contentStream = chatClient
            .prompt(createPrompt(simpleResponseDeploymentName))
            .toolCallbacks(weatherTool)
            .stream()
            .content()

        val result = contentStream
            .collectList()
            .block()

        assertEquals(expectedResult, result?.joinToString("\n"))
    }

    @RepeatedTest(20000, failureThreshold = 1)
    fun `tool call response scenario - order should not be messed up`(repetitionInfo: RepetitionInfo) {
        println("TC - Repetition ${repetitionInfo.currentRepetition} of ${repetitionInfo.totalRepetitions}")
        WireMock.resetAllScenarios()
        if (repetitionInfo.currentRepetition % 50 == 0) {
            WireMock.reset()
            System.gc()
        }

        val contentStream = chatClient
            .prompt(createPrompt(scenarioToolCallDeploymentName))
            .toolCallbacks(weatherTool)
            .stream()
            .content()

        val result = contentStream
            .collectList()
            .block()

        assertEquals(expectedResult, result?.joinToString("\n"))
    }

    private val simpleResponseDeploymentName = "stubbed"
    private val scenarioToolCallDeploymentName = "stubbed-scenario"

    private fun createPrompt(deploymentName: String) =
        Prompt.builder()
            .content("Test")
            .chatOptions(
                AzureOpenAiChatOptions
                    .builder()
                    .deploymentName(deploymentName)
                    .build(),
            )
            .build()
}
