package com.example.ai

import android.content.Context
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Adaptador real de la API de Gemini (generateContent) con soporte de
 * function calling: las herramientas se declaran con su esquema JSON y el
 * modelo devuelve llamadas de función estructuradas, no bloques de texto.
 */
class GeminiProvider(private val context: Context) : AIProvider {

    companion object {
        const val DEFAULT_MODEL = "gemini-2.5-flash"
        private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"
        const val ID = "gemini"
        const val DISPLAY_NAME = "Google Gemini"
    }

    override val id: String = ID
    override val displayName: String = DISPLAY_NAME
    override val defaultModel: String = DEFAULT_MODEL
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        setOf(ProviderCapability.TEXT, ProviderCapability.TOOL_CALLING, ProviderCapability.STREAMING, ProviderCapability.VISION, ProviderCapability.FILE_INPUT)
    )

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter: JsonAdapter<WireRequest> = moshi.adapter(WireRequest::class.java)
    private val responseAdapter: JsonAdapter<WireResponse> = moshi.adapter(WireResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    override fun isConfigured(): Boolean = AiKeyProvider.apiKey() != null

    override suspend fun testConnection(): ProviderTestResult = withContext(Dispatchers.IO) {
        val key = AiKeyProvider.apiKey()
            ?: return@withContext ProviderTestResult(
                false,
                "sin GEMINI_API_KEY: defínela en el archivo .env del proyecto y recompila"
            )
        val request = WireRequest(
            contents = listOf(WireContent(role = "user", parts = listOf(WirePart(text = "ping")))),
            generationConfig = WireGenerationConfig(maxOutputTokens = 16)
        )
        try {
            val http = Request.Builder()
                .url("${ENDPOINT}${DEFAULT_MODEL}:generateContent?key=$key")
                .post(requestAdapter.toJson(request).toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(http).execute().use { response ->
                if (response.isSuccessful) {
                    ProviderTestResult(true, "conexión correcta con ${DEFAULT_MODEL}")
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
        val key = AiKeyProvider.apiKey()
            ?: return@withContext ChatResponse.Error(
                "sin GEMINI_API_KEY: defínela en el archivo .env del proyecto y recompila"
            )

        val wireTools = if (request.tools.isEmpty()) null else request.tools.map { declaration ->
            val schema: Map<String, Any?> = try {
                @Suppress("UNCHECKED_CAST")
                moshi.adapter(Map::class.java).fromJson(declaration.parametersSchema)
                    ?.entries?.associate { (k, v) -> k.toString() to v }
                    ?: emptyMap()
            } catch (_: Throwable) {
                emptyMap()
            }
            WireTool(listOf(WireFunctionDeclaration(declaration.name, declaration.description, schema)))
        }

        val wireRequest = WireRequest(
            contents = request.contents.map { content ->
                WireContent(
                    role = content.role,
                    parts = content.parts.map { part ->
                        when (part) {
                            is ChatPart.Text -> WirePart(text = part.text)
                            is ChatPart.FunctionCall ->
                                WirePart(
                                    functionCall = WireFunctionCallPayload(
                                        part.name,
                                        try {
                                            @Suppress("UNCHECKED_CAST")
                                            moshi.adapter(Map::class.java).fromJson(part.argumentsJson) as? Map<String, Any>
                                                ?: emptyMap()
                                        } catch (_: Throwable) {
                                            emptyMap()
                                        }
                                    )
                                )
                            is ChatPart.FunctionResponse ->
                                WirePart(
                                    functionResponse = WireFunctionResponsePayload(
                                        part.name,
                                        mapOf("result" to part.responseJson)
                                    )
                                )
                        }
                    }
                )
            },
            systemInstruction = WireContent(role = null, parts = listOf(WirePart(text = request.systemPrompt))),
            tools = wireTools,
            generationConfig = WireGenerationConfig(
                temperature = request.temperature,
                maxOutputTokens = request.maxOutputTokens
            )
        )

        try {
            val http = Request.Builder()
                .url("${ENDPOINT}${request.model}:generateContent?key=$key")
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
                val candidate = parsed.candidates?.firstOrNull()
                val parts = candidate?.content?.parts.orEmpty()
                if (parts.isEmpty()) {
                    return@withContext ChatResponse.Error(
                        candidate?.finishReason?.let { "el modelo no devolvió contenido (motivo: $it)" }
                            ?: "el modelo no devolvió contenido"
                    )
                }
                ChatResponse.Ok(
                    parts = parts.map { part ->
                        when {
                            part.functionCall != null -> ChatPart.FunctionCall(
                                part.functionCall.name.orEmpty(),
                                part.functionCall.args?.let { moshi.adapter(Map::class.java).toJson(it) } ?: "{}"
                            )
                            else -> ChatPart.Text(part.text.orEmpty())
                        }
                    },
                    finishReason = candidate?.finishReason
                )
            }
        } catch (e: Throwable) {
            ChatResponse.Error("sin conexión con el servicio de IA (${e.javaClass.simpleName}: ${e.message ?: "sin detalle"})")
        }
    }
}

@JsonClass(generateAdapter = false)
data class WireRequest(
    val contents: List<WireContent>,
    val systemInstruction: WireContent? = null,
    val tools: List<WireTool>? = null,
    val generationConfig: WireGenerationConfig? = null
)

@JsonClass(generateAdapter = false)
data class WireGenerationConfig(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    @Json(name = "response_mime_type") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = false)
data class WireContent(val role: String?, val parts: List<WirePart>)

@JsonClass(generateAdapter = false)
data class WirePart(
    val text: String? = null,
    @Json(name = "functionCall") val functionCall: WireFunctionCallPayload? = null,
    @Json(name = "functionResponse") val functionResponse: WireFunctionResponsePayload? = null
)

@JsonClass(generateAdapter = false)
data class WireFunctionCallPayload(
    val name: String?,
    val args: Map<String, Any>? = null
)

@JsonClass(generateAdapter = false)
data class WireFunctionResponsePayload(
    val name: String?,
    val response: Map<String, Any>
)

@JsonClass(generateAdapter = false)
data class WireTool(val functionDeclarations: List<WireFunctionDeclaration>)

@JsonClass(generateAdapter = false)
data class WireFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Map<String, Any?>
)

@JsonClass(generateAdapter = false)
data class WireResponse(
    val candidates: List<WireCandidate>? = null,
    val error: WireApiError? = null
)

@JsonClass(generateAdapter = false)
data class WireCandidate(val content: WireCandidateContent? = null, val finishReason: String? = null)

@JsonClass(generateAdapter = false)
data class WireCandidateContent(val parts: List<WirePart>? = null)

@JsonClass(generateAdapter = false)
data class WireApiError(val code: Int? = null, val message: String? = null)
