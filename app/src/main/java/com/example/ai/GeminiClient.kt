package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente real de la API de Gemini (generateContent). Sin respuestas
 * prefabricadas: cada respuesta proviene del modelo, y los comandos que el
 * agente propone se extraen de bloques ```run del texto devuelto.
 */
object GeminiClient {

    const val DEFAULT_MODEL = "gemini-2.5-flash"

    private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val requestAdapter: JsonAdapter<GenerateRequest> = moshi.adapter(GenerateRequest::class.java)
    private val responseAdapter: JsonAdapter<GenerateResponse> = moshi.adapter(GenerateResponse::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    data class ChatTurn(val isUser: Boolean, val text: String)

    data class AgentReply(
        val text: String,
        val proposedCommand: String?,
        val quickReplies: List<String>
    )

    sealed class Result {
        data class Success(val reply: AgentReply) : Result()
        data class Failure(val reason: String) : Result()
    }

    suspend fun generate(
        agentName: String,
        agentRole: String,
        agentDescription: String,
        history: List<ChatTurn>,
        userMessage: String,
        projectContext: String? = null
    ): Result = withContext(Dispatchers.IO) {
        val apiKey = AiKeyProvider.apiKey()
            ?: return@withContext Result.Failure(
                "falta la clave de la API de Gemini. Defínela como GEMINI_API_KEY en el archivo .env del proyecto y vuelve a compilar."
            )

        val contents = history.map { turn ->
            Content(role = if (turn.isUser) "user" else "model", parts = listOf(Part(turn.text)))
        } + Content(role = "user", parts = listOf(Part(userMessage)))

        val request = GenerateRequest(
            contents = contents,
            systemInstruction = Content(
                role = null,
                parts = listOf(Part(systemPrompt(agentName, agentRole, agentDescription, projectContext)))
            ),
            generationConfig = GenerationConfig()
        )

        try {
            val httpRequest = Request.Builder()
                .url("${ENDPOINT}${DEFAULT_MODEL}:generateContent?key=$apiKey")
                .post(requestAdapter.toJson(request).toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(httpRequest).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val apiError = responseAdapter.fromJson(raw)?.error
                    return@withContext Result.Failure(
                        "el servicio de IA respondió HTTP ${response.code}${apiError?.message?.let { ": $it" } ?: ""}"
                    )
                }
                val parsed = responseAdapter.fromJson(raw)
                    ?: return@withContext Result.Failure("la respuesta del servicio de IA no se pudo interpretar")

                val text = parsed.candidates?.firstOrNull()?.content?.parts
                    ?.joinToString("") { it.text }?.trim().orEmpty()
                if (text.isEmpty()) {
                    val finish = parsed.candidates?.firstOrNull()?.finishReason
                    return@withContext Result.Failure(
                        if (finish != null) "el modelo no devolvió contenido (motivo: $finish)"
                        else "el modelo no devolvió contenido"
                    )
                }
                Result.Success(parseReply(text))
            }
        } catch (e: Throwable) {
            Result.Failure("no hubo conexión con el servicio de IA (${e.javaClass.simpleName}: ${e.message ?: "sin detalle"})")
        }
    }

    private fun systemPrompt(
        agentName: String,
        agentRole: String,
        agentDescription: String,
        projectContext: String?
    ): String = buildString {
        appendLine("Eres $agentName, el agente $agentRole de CatProjectAgent. $agentDescription")
        appendLine()
        appendLine(
            "Entorno real disponible: Ubuntu 24.04 ejecutándose con PRoot dentro del dispositivo Android del usuario. " +
                "Los comandos que el usuario apruebe se ejecutan en /bin/bash de ese entorno y su salida real se devuelve a la conversación. " +
                "El workspace compartido con el usuario está montado en /root/workspace."
        )
        appendLine()
        appendLine("Reglas:")
        appendLine("- Responde en el idioma del usuario, de forma breve y concreta.")
        appendLine(
            "- Si necesitas ejecutar algo en el entorno, incluye un único bloque ```run``` con un comando bash por línea (máximo 5 líneas). " +
                "La ejecución es real: no inventes ni anticipes resultados."
        )
        appendLine("- Si quieres ofrecer atajos al usuario, añade un bloque ```suggest``` con hasta 3 respuestas cortas, una por línea.")
        appendLine("- Nunca digas que ya ejecutaste algo: toda ejecución requiere la aprobación previa del usuario.")
        projectContext?.takeIf { it.isNotBlank() }?.let {
            appendLine()
            append("Proyecto activo: $it")
        }
    }

    private val RUN_BLOCK = Regex("```run\\s*\\n([\\s\\S]*?)```", RegexOption.IGNORE_CASE)
    private val SUGGEST_BLOCK = Regex("```suggest\\s*\\n([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    private fun parseReply(raw: String): AgentReply {
        val command = RUN_BLOCK.find(raw)?.groupValues?.get(1)
            ?.lines()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() && !it.startsWith("#") }
            ?.joinToString("\n")
            ?.takeIf { it.isNotEmpty() }
        val suggestions = SUGGEST_BLOCK.find(raw)?.groupValues?.get(1)
            ?.lines()
            ?.map { it.trim().trimStart('-', '*', '•', ' ') }
            ?.filter { it.isNotEmpty() }
            ?.take(3)
            .orEmpty()
        val text = raw.replace(RUN_BLOCK, "").replace(SUGGEST_BLOCK, "").trim()
        return AgentReply(text.ifBlank { "Listo." }, command, suggestions)
    }
}

data class GenerateRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null,
    val generationConfig: GenerationConfig? = null
)

data class GenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 2048
)

data class Content(
    val role: String?,
    val parts: List<Part>
)

data class Part(val text: String)

data class GenerateResponse(
    val candidates: List<Candidate>? = null,
    val error: ApiError? = null
)

data class Candidate(
    val content: CandidateContent? = null,
    val finishReason: String? = null
)

data class CandidateContent(val parts: List<Part>? = null)

data class ApiError(
    val code: Int? = null,
    val message: String? = null
)
