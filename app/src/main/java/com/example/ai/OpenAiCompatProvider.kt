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
 * Adaptador real para las APIs compatibles con el formato Chat Completions
 * de OpenAI. Cubre cuatro proveedores distintos que comparten el mismo wire
 * format: OpenAI, Mistral, Kimi (Moonshot) y DeepSeek. Todos con soporte de
 * function calling: las herramientas se declaran con su esquema JSON y el
 * modelo devuelve tool_calls estructuradas que AgentEngine ejecuta de verdad.
 */
class OpenAiCompatProvider private constructor(
    override val id: String,
    override val displayName: String,
    override val defaultModel: String,
    private val baseUrl: String,
    private val apiKey: String?
) : AIProvider {

    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        setOf(ProviderCapability.TEXT, ProviderCapability.TOOL_CALLING)
    )

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter: JsonAdapter<OaiRequest> = moshi.adapter(OaiRequest::class.java)
    private val responseAdapter: JsonAdapter<OaiResponse> = moshi.adapter(OaiResponse::class.java)
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
        val request = OaiRequest(
            model = defaultModel,
            messages = listOf(OaiMessage(role = "user", content = "ping")),
            maxTokens = 8
        )
        try {
            val http = Request.Builder()
                .url("$baseUrl/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(requestAdapter.toJson(request).toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(http).execute().use { response ->
                if (response.isSuccessful) {
                    ProviderTestResult(true, "conexión correcta con $defaultModel")
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
            OaiTool(function = OaiToolDeclaration(declaration.name, declaration.description, schema))
        }

        val wireRequest = OaiRequest(
            model = request.model,
            messages = toWireMessages(request.systemPrompt, request.contents),
            tools = wireTools,
            temperature = request.temperature,
            maxTokens = request.maxOutputTokens
        )

        try {
            val http = Request.Builder()
                .url("$baseUrl/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .post(requestAdapter.toJson(wireRequest).toRequestBody("application/json".toMediaType()))
                .build()
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
                val choice = parsed.choices?.firstOrNull()
                val message = choice?.message
                if (message == null || (message.content.isNullOrBlank() && message.toolCalls.isNullOrEmpty())) {
                    return@withContext ChatResponse.Error(
                        choice?.finishReason?.let { "el modelo no devolvió contenido (motivo: $it)" }
                            ?: "el modelo no devolvió contenido"
                    )
                }
                val parts = mutableListOf<ChatPart>()
                message.content?.takeIf { it.isNotBlank() }?.let { parts.add(ChatPart.Text(it)) }
                message.toolCalls?.forEach { call ->
                    parts.add(ChatPart.FunctionCall(call.function.name.orEmpty(), call.function.arguments.orEmpty().ifBlank { "{}" }))
                }
                ChatResponse.Ok(parts = parts, finishReason = choice.finishReason)
            }
        } catch (e: Throwable) {
            ChatResponse.Error("sin conexión con el servicio de IA (${e.javaClass.simpleName}: ${e.message ?: "sin detalle"})")
        }
    }

    /**
     * Convierte la conversación genérica (roles user/model, partes Text,
     * FunctionCall y FunctionResponse) al formato de mensajes de OpenAI.
     * Los tool_call_id se asignan en orden y se emparejan con las respuestas
     * de herramienta por nombre, tal como exige el protocolo.
     */
    private fun toWireMessages(systemPrompt: String, contents: List<Content>): List<OaiMessage> {
        val messages = mutableListOf<OaiMessage>()
        if (systemPrompt.isNotBlank()) {
            messages.add(OaiMessage(role = "system", content = systemPrompt))
        }
        var callCounter = 0
        val pendingIds = ArrayDeque<Pair<String, String>>() // nombre de función → tool_call_id

        contents.forEach { content ->
            val role = if (content.role == "model") "assistant" else "user"
            val text = content.parts.filterIsInstance<ChatPart.Text>()
                .joinToString("\n") { it.text }
                .takeIf { it.isNotBlank() }
            val calls = content.parts.filterIsInstance<ChatPart.FunctionCall>()
            val responses = content.parts.filterIsInstance<ChatPart.FunctionResponse>()

            when {
                calls.isNotEmpty() -> {
                    val toolCalls = calls.map { call ->
                        callCounter++
                        val id = "call_$callCounter"
                        pendingIds.add(call.name to id)
                        OaiToolCall(id = id, function = OaiToolFn(name = call.name, arguments = call.argumentsJson))
                    }
                    messages.add(OaiMessage(role = "assistant", content = text, toolCalls = toolCalls))
                }
                responses.isNotEmpty() -> {
                    responses.forEach { response ->
                        val match = pendingIds.firstOrNull { it.first == response.name }
                        if (match != null) pendingIds.remove(match)
                        messages.add(
                            OaiMessage(
                                role = "tool",
                                content = response.responseJson,
                                toolCallId = match?.second ?: "call_missing",
                                // El ToolMessage de Mistral documenta el campo name de la función;
                                // el resto de proveedores compatibles lo ignoran, así que solo se
                                // envía donde forma parte del contrato real.
                                name = response.name.takeIf { id == MISTRAL_ID }
                            )
                        )
                    }
                    if (text != null) messages.add(OaiMessage(role = role, content = text))
                }
                else -> messages.add(OaiMessage(role = role, content = text))
            }
        }
        return messages
    }

    companion object {
        const val OPENAI_ID = "openai"
        const val MISTRAL_ID = "mistral"
        const val KIMI_ID = "kimi"
        const val DEEPSEEK_ID = "deepseek"

        fun openai(apiKey: String?) = OpenAiCompatProvider(
            OPENAI_ID, "OpenAI", "gpt-4o-mini", "https://api.openai.com/v1", apiKey
        )

        fun mistral(apiKey: String?) = OpenAiCompatProvider(
            MISTRAL_ID, "Mistral AI", "mistral-small-latest", "https://api.mistral.ai/v1", apiKey
        )

        fun kimi(apiKey: String?) = OpenAiCompatProvider(
            KIMI_ID, "Kimi (Moonshot)", "kimi-k2-turbo-preview", "https://api.moonshot.ai/v1", apiKey
        )

        fun deepseek(apiKey: String?) = OpenAiCompatProvider(
            DEEPSEEK_ID, "DeepSeek", "deepseek-chat", "https://api.deepseek.com/v1", apiKey
        )
    }
}

@JsonClass(generateAdapter = false)
data class OaiRequest(
    val model: String,
    val messages: List<OaiMessage>,
    val tools: List<OaiTool>? = null,
    val temperature: Double? = null,
    @Json(name = "max_tokens") val maxTokens: Int? = null
)

@JsonClass(generateAdapter = false)
data class OaiMessage(
    val role: String,
    val content: String? = null,
    @Json(name = "tool_calls") val toolCalls: List<OaiToolCall>? = null,
    @Json(name = "tool_call_id") val toolCallId: String? = null,
    val name: String? = null
)

@JsonClass(generateAdapter = false)
data class OaiToolCall(
    // Mistral puede devolver tool_calls sin id: se deja opcional para no
    // rechazar respuestas reales del servicio.
    val id: String? = null,
    @Json(name = "type") val type: String = "function",
    val function: OaiToolFn
)

@JsonClass(generateAdapter = false)
data class OaiToolFn(val name: String?, val arguments: String? = null)

@JsonClass(generateAdapter = false)
data class OaiTool(
    @Json(name = "type") val type: String = "function",
    val function: OaiToolDeclaration
)

@JsonClass(generateAdapter = false)
data class OaiToolDeclaration(
    val name: String,
    val description: String,
    val parameters: Map<String, Any?>
)

@JsonClass(generateAdapter = false)
data class OaiResponse(
    val choices: List<OaiChoice>? = null,
    val error: OaiError? = null
)

@JsonClass(generateAdapter = false)
data class OaiChoice(
    val message: OaiMessageOut? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = false)
data class OaiMessageOut(
    val role: String? = null,
    val content: String? = null,
    @Json(name = "tool_calls") val toolCalls: List<OaiToolCall>? = null
)

@JsonClass(generateAdapter = false)
data class OaiError(val message: String? = null, val type: String? = null)
