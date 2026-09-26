package com.example.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Adaptador real de la API Messages de Anthropic (Claude) con soporte de
 * tool use: las herramientas se declaran con input_schema y el modelo
 * devuelve bloques tool_use estructurados que AgentEngine ejecuta de verdad
 * contra el ToolGateway. Formato de wire propio de Anthropic: system va a
 * nivel raíz y los resultados de herramientas son bloques tool_result.
 */
class ClaudeProvider(private val apiKey: String?) : AIProvider {

    companion object {
        const val ID = "claude"
        const val DISPLAY_NAME = "Anthropic Claude"
        const val DEFAULT_MODEL = "claude-sonnet-4-5"
        private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
        private const val API_VERSION = "2023-06-01"
    }

    override val id: String = ID
    override val displayName: String = DISPLAY_NAME
    override val defaultModel: String = DEFAULT_MODEL
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        setOf(ProviderCapability.TEXT, ProviderCapability.TOOL_CALLING, ProviderCapability.VISION)
    )

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter: JsonAdapter<ClaudeRequest> = moshi.adapter(ClaudeRequest::class.java)
    private val responseAdapter: JsonAdapter<ClaudeResponse> = moshi.adapter(ClaudeResponse::class.java)
    private val mapAdapter: JsonAdapter<Map<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    override fun isConfigured(): Boolean = !apiKey.isNullOrBlank()

    override suspend fun testConnection(): ProviderTestResult = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) {
            return@withContext ProviderTestResult(false, "sin clave de API: introdúcela en Ajustes → Cerebros de IA")
        }
        val request = ClaudeRequest(
            model = DEFAULT_MODEL,
            maxTokens = 8,
            messages = listOf(ClaudeMessage(role = "user", content = listOf(ClaudeBlock(type = "text", text = "ping"))))
        )
        try {
            val http = buildHttp(request)
            client.newCall(http).execute().use { response ->
                if (response.isSuccessful) {
                    ProviderTestResult(true, "conexión correcta con $DEFAULT_MODEL")
                } else {
                    val err = responseAdapter.fromJson(response.body?.string().orEmpty())?.error?.message
                    ProviderTestResult(false, "HTTP ${response.code}${err?.let { ": $it" } ?: ""}")
                }
            }
        } catch (e: Throwable) {
            ProviderTestResult(false, "sin conexión (${e.javaClass.simpleName}: ${e.message ?: "sin detalle"})")
        }
    }

    override suspend fun chat(request: ChatRequest): ChatResponse = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) {
            return@withContext ChatResponse.Error("sin clave de API: introdúcela en Ajustes → Cerebros de IA")
        }

        val wireTools = if (request.tools.isEmpty()) null else request.tools.map { declaration ->
            val schema: Map<String, Any?> = try {
                @Suppress("UNCHECKED_CAST")
                mapAdapter.fromJson(declaration.parametersSchema)
                    ?.entries?.associate { (k, v) -> k.toString() to v }
                    ?: emptyMap()
            } catch (_: Throwable) {
                emptyMap()
            }
            ClaudeTool(name = declaration.name, description = declaration.description, inputSchema = schema)
        }

        val wireRequest = ClaudeRequest(
            model = request.model,
            maxTokens = request.maxOutputTokens,
            system = request.systemPrompt.takeIf { it.isNotBlank() },
            tools = wireTools,
            temperature = request.temperature,
            messages = toWireMessages(request.contents)
        )

        try {
            val http = buildHttp(wireRequest)
            client.newCall(http).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val apiError = responseAdapter.fromJson(raw)?.error
                    return@withContext ChatResponse.Error(
                        "HTTP ${response.code}${apiError?.message?.let { ": $it" } ?: ""}"
                    )
                }
                val parsed = responseAdapter.fromJson(raw)
                    ?: return@withContext ChatResponse.Error("respuesta ilegible del servicio")
                val blocks = parsed.content.orEmpty()
                if (blocks.isEmpty()) {
                    return@withContext ChatResponse.Error(
                        parsed.stopReason?.let { "el modelo no devolvió contenido (motivo: $it)" }
                            ?: "el modelo no devolvió contenido"
                    )
                }
                val parts = mutableListOf<ChatPart>()
                blocks.forEach { block ->
                    when (block.type) {
                        "tool_use" -> parts.add(
                            ChatPart.FunctionCall(
                                block.name.orEmpty(),
                                block.input?.let { mapAdapter.toJson(it) } ?: "{}"
                            )
                        )
                        "text" -> if (!block.text.isNullOrBlank()) parts.add(ChatPart.Text(block.text))
                    }
                }
                if (parts.isEmpty()) {
                    return@withContext ChatResponse.Error("el modelo no devolvió contenido utilizable")
                }
                ChatResponse.Ok(parts = parts, finishReason = parsed.stopReason)
            }
        } catch (e: Throwable) {
            ChatResponse.Error("sin conexión con el servicio de IA (${e.javaClass.simpleName}: ${e.message ?: "sin detalle"})")
        }
    }

    private fun buildHttp(body: ClaudeRequest): Request = Request.Builder()
        .url(ENDPOINT)
        .header("x-api-key", apiKey.orEmpty())
        .header("anthropic-version", API_VERSION)
        .post(requestAdapter.toJson(body).toRequestBody("application/json".toMediaType()))
        .build()

    /**
     * Convierte la conversación genérica al formato Messages de Anthropic.
     * Los tool_use del asistente llevan ids que se emparejan con los
     * tool_result del turno user siguiente, por nombre, como exige el
     * protocolo de Anthropic.
     */
    private fun toWireMessages(contents: List<Content>): List<ClaudeMessage> {
        val messages = mutableListOf<ClaudeMessage>()
        var toolCounter = 0
        val pendingIds = ArrayDeque<Pair<String, String>>() // nombre de función → tool_use id

        contents.forEach { content ->
            val role = if (content.role == "model") "assistant" else "user"
            val texts = content.parts.filterIsInstance<ChatPart.Text>()
            val calls = content.parts.filterIsInstance<ChatPart.FunctionCall>()
            val responses = content.parts.filterIsInstance<ChatPart.FunctionResponse>()
            val blocks = mutableListOf<ClaudeBlock>()

            when {
                calls.isNotEmpty() -> {
                    texts.forEach { blocks.add(ClaudeBlock(type = "text", text = it.text)) }
                    calls.forEach { call ->
                        toolCounter++
                        val id = "toolu_cpa_$toolCounter"
                        pendingIds.add(call.name to id)
                        val input: Map<String, Any?> = try {
                            @Suppress("UNCHECKED_CAST")
                            mapAdapter.fromJson(call.argumentsJson) as? Map<String, Any?> ?: emptyMap()
                        } catch (_: Throwable) {
                            emptyMap()
                        }
                        blocks.add(ClaudeBlock(type = "tool_use", id = id, name = call.name, input = input))
                    }
                    messages.add(ClaudeMessage(role = "assistant", content = blocks.toList()))
                }
                responses.isNotEmpty() -> {
                    responses.forEach { response ->
                        val match = pendingIds.firstOrNull { it.first == response.name }
                        if (match != null) pendingIds.remove(match)
                        blocks.add(
                            ClaudeBlock(
                                type = "tool_result",
                                toolUseId = match?.second ?: "toolu_missing",
                                content = response.responseJson
                            )
                        )
                    }
                    texts.forEach { blocks.add(ClaudeBlock(type = "text", text = it.text)) }
                    messages.add(ClaudeMessage(role = "user", content = blocks.toList()))
                }
                else -> {
                    val text = texts.joinToString("\n") { it.text }
                    if (text.isNotBlank()) {
                        messages.add(
                            ClaudeMessage(role = role, content = listOf(ClaudeBlock(type = "text", text = text)))
                        )
                    }
                }
            }
        }
        return messages
    }
}

@JsonClass(generateAdapter = false)
data class ClaudeRequest(
    val model: String,
    @Json(name = "max_tokens") val maxTokens: Int,
    val system: String? = null,
    val messages: List<ClaudeMessage>,
    val tools: List<ClaudeTool>? = null,
    val temperature: Double? = null
)

@JsonClass(generateAdapter = false)
data class ClaudeMessage(val role: String, val content: List<ClaudeBlock>)

@JsonClass(generateAdapter = false)
data class ClaudeBlock(
    val type: String,
    val text: String? = null,
    val id: String? = null,
    val name: String? = null,
    val input: Map<String, Any?>? = null,
    @Json(name = "tool_use_id") val toolUseId: String? = null,
    val content: String? = null
)

@JsonClass(generateAdapter = false)
data class ClaudeTool(
    val name: String,
    val description: String,
    @Json(name = "input_schema") val inputSchema: Map<String, Any?>
)

@JsonClass(generateAdapter = false)
data class ClaudeResponse(
    val content: List<ClaudeBlock>? = null,
    @Json(name = "stop_reason") val stopReason: String? = null,
    val error: ClaudeApiError? = null
)

@JsonClass(generateAdapter = false)
data class ClaudeApiError(val type: String? = null, val message: String? = null)
