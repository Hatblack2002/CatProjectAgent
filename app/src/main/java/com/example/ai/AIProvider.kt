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

/** Descriptor real de un proveedor: lo que la UI muestra y ofrece elegir. */
data class ProviderDescriptor(
    val id: String,
    val displayName: String,
    val defaultModel: String,
    val models: List<String>,
    val keyHint: String,
    val isPrimaryBrain: Boolean = false
)

/**
 * Registro de proveedores disponibles: solo integraciones reales. Cada
 * proveedor se instancia con la clave de su slot, así dos agentes pueden
 * incluso usar el mismo proveedor con claves distintas.
 */
object ProviderRegistry {

    val descriptors: List<ProviderDescriptor> = listOf(
        ProviderDescriptor(
            id = GeminiProvider.ID,
            displayName = "Google Gemini",
            defaultModel = GeminiProvider.DEFAULT_MODEL,
            models = listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-flash-lite"),
            keyHint = "Clave de Google AI Studio (empieza por AIza…)",
            isPrimaryBrain = true
        ),
        ProviderDescriptor(
            id = OpenAiCompatProvider.OPENAI_ID,
            displayName = "OpenAI",
            defaultModel = "gpt-4o-mini",
            models = listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1", "gpt-4.1-mini", "gpt-4.1-nano"),
            keyHint = "Clave de platform.openai.com (empieza por sk-…)"
        ),
        ProviderDescriptor(
            id = ClaudeProvider.ID,
            displayName = ClaudeProvider.DISPLAY_NAME,
            defaultModel = ClaudeProvider.DEFAULT_MODEL,
            models = listOf("claude-sonnet-4-5", "claude-haiku-4-5", "claude-opus-4-1"),
            keyHint = "Clave de console.anthropic.com (empieza por sk-ant-…)"
        ),
        ProviderDescriptor(
            id = OpenAiCompatProvider.MISTRAL_ID,
            displayName = "Mistral AI",
            defaultModel = "mistral-small-latest",
            models = listOf("mistral-small-latest", "mistral-medium-latest", "mistral-large-latest", "devstral-medium-latest"),
            keyHint = "Clave de console.mistral.ai"
        ),
        ProviderDescriptor(
            id = OpenAiCompatProvider.KIMI_ID,
            displayName = "Kimi (Moonshot)",
            defaultModel = "kimi-k2-turbo-preview",
            models = listOf("kimi-k2-turbo-preview", "kimi-k2-preview", "moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"),
            keyHint = "Clave de platform.moonshot.ai (empieza por sk-…)"
        ),
        ProviderDescriptor(
            id = OpenAiCompatProvider.DEEPSEEK_ID,
            displayName = "DeepSeek",
            defaultModel = "deepseek-chat",
            models = listOf("deepseek-chat", "deepseek-reasoner"),
            keyHint = "Clave de platform.deepseek.com (empieza por sk-…)"
        )
    )

    fun descriptor(id: String): ProviderDescriptor? = descriptors.firstOrNull { it.id == id }

    /** Crea un proveedor real ligado a la clave del slot que lo pide. */
    fun create(context: android.content.Context, providerId: String, apiKey: String?): AIProvider =
        when (providerId) {
            GeminiProvider.ID -> GeminiProvider(context.applicationContext, apiKey)
            ClaudeProvider.ID -> ClaudeProvider(apiKey)
            OpenAiCompatProvider.OPENAI_ID -> OpenAiCompatProvider.openai(apiKey)
            OpenAiCompatProvider.MISTRAL_ID -> OpenAiCompatProvider.mistral(apiKey)
            OpenAiCompatProvider.KIMI_ID -> OpenAiCompatProvider.kimi(apiKey)
            OpenAiCompatProvider.DEEPSEEK_ID -> OpenAiCompatProvider.deepseek(apiKey)
            else -> GeminiProvider(context.applicationContext, apiKey)
        }
}
