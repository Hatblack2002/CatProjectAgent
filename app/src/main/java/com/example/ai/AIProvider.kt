package com.example.ai

/**
 * Abstracción de proveedores de IA. Un proveedor solo se expone en la UI
 * cuando existe integración real, puede configurarse, probarse y producir
 * respuestas reales.
 */
enum class ProviderCapability {
    TEXT,
    TOOL_CALLING,
    STREAMING,
    VISION,
    FILE_INPUT
}

data class ProviderCapabilities(val supported: Set<ProviderCapability>) {
    fun has(capability: ProviderCapability) = capability in supported
}

data class ProviderTestResult(val ok: Boolean, val detail: String)

interface AIProvider {
    val id: String
    val displayName: String
    val defaultModel: String
    val capabilities: ProviderCapabilities

    /** True cuando existe la credencial/configuración necesaria. */
    fun isConfigured(): Boolean

    /** Prueba de conexión real contra la API del proveedor. */
    suspend fun testConnection(): ProviderTestResult

    /** Envía una conversación completa y devuelve la respuesta cruda del modelo. */
    suspend fun chat(request: ChatRequest): ChatResponse
}

data class ChatRequest(
    val model: String,
    val systemPrompt: String,
    val contents: List<Content>,
    val tools: List<ToolDeclaration> = emptyList(),
    val maxOutputTokens: Int = 4096,
    val temperature: Double = 0.6
)

data class ToolDeclaration(
    val name: String,
    val description: String,
    val parametersSchema: String
)

sealed class ChatPart {
    data class Text(val text: String) : ChatPart()
    data class FunctionCall(val name: String, val argumentsJson: String) : ChatPart()
    data class FunctionResponse(val name: String, val responseJson: String) : ChatPart()
}

data class Content(val role: String, val parts: List<ChatPart>)

sealed class ChatResponse {
    data class Ok(val parts: List<ChatPart>, val finishReason: String?) : ChatResponse()
    data class Error(val reason: String) : ChatResponse()
}

/** Registro de proveedores disponibles: solo integraciones reales. */
object ProviderRegistry {

    @Volatile
    private var gemini: GeminiProvider? = null

    fun gemini(context: android.content.Context): GeminiProvider =
        gemini ?: GeminiProvider(context.applicationContext).also { gemini = it }

    fun available(context: android.content.Context): List<AIProvider> = listOf(gemini(context))
}
