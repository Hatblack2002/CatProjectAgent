package com.example.agents

import com.example.ai.ChatPart
import com.example.ai.ChatRequest
import com.example.ai.ChatResponse
import com.example.ai.Content
import com.example.ai.ResolvedBrain
import com.example.model.CatAgent
import com.example.tools.JsonArgs
import com.example.tools.ToolGateway
import com.example.tools.ToolGateway.ToolCall

/**
 * Motor de agentes: ejecuta el bucle real modelo → functionCall → ToolGateway
 * → functionResponse → modelo hasta obtener respuesta final o agotar el
 * máximo de iteraciones. Comparte el estado del proyecto a través de las
 * herramientas reales (workspace, tareas, issues), nunca por inventos.
 *
 * Cada agente se atiende con su cerebro resuelto: slot propio si tiene uno,
 * cerebro principal si no, y Gemini compilado como último recurso.
 */
class AgentEngine(
    private val brainFor: (CatAgent) -> ResolvedBrain,
    private val gateway: ToolGateway,
    private val listener: Listener
) {

    interface Listener {
        /** Cada uso de herramienta se informa para mostrarlo en la UI. */
        fun onToolExecution(agentName: String, tool: String, summary: String)
    }

    data class AgentTurn(
        val text: String,
        val toolExecutions: Int,
        val ok: Boolean,
        val errorReason: String? = null
    )

    companion object {
        private const val MAX_ITERATIONS = 10
        private const val MAX_TOKENS = 4096

        fun rolePrompt(agent: CatAgent, activeProject: String?): String = buildString {
            appendLine("Eres ${agent.name}, agente ${agent.role} de CatProjectAgent. ${agent.description}")
            appendLine()
            appendLine("Tienes herramientas REALES que actúan sobre el dispositivo del usuario:")
            appendLine("- inspect_project / list_directory / read_file: leen el workspace real del usuario.")
            appendLine("- write_file: crea archivos nuevos o reescribe uno completo.")
            appendLine("- edit_file: edita archivos existentes con bloques SEARCH/REPLACE exactos:")
            appendLine("  <<<<<<< SEARCH / texto exacto actual / ======= / texto nuevo / >>>>>>> REPLACE.")
            appendLine("  Es el método preferido para modificar código: nunca reescribas un archivo entero si solo cambia una parte.")
            appendLine("- run_process: ejecuta comandos reales en un entorno Linux (Ubuntu 24.04) integrado en el dispositivo.")
            appendLine("- install_dependency: instala dependencias reales (apt, pip, gradle).")
            appendLine("- build_project / run_tests: compilan y prueban el proyecto real; su resultado es verificable.")
            appendLine("- create_task / complete_task: planifican y actualizan tareas reales del proyecto.")
            appendLine("- report_issue: registra problemas verificables con evidencia.")
            appendLine()
            appendLine("Reglas absolutas:")
            appendLine("- Trabaja sobre archivos y comandos reales usando las herramientas. Nunca afirmes que hiciste algo que no ejecutaste con una herramienta.")
            appendLine("- Antes de modificar código, inspecciona el proyecto real (inspect_project, read_file) y cita rutas reales.")
            appendLine("- Las acciones sensibles requieren aprobación del usuario: si la rechaza, no insistas.")
            appendLine("- Cuando termines un trabajo, resume qué archivos tocaste y qué evidencia dejó tu ejecución (build exitoso, tests, rutas).")
            appendLine("- Responde en el idioma del usuario, breve y concreto.")
            activeProject?.let {
                appendLine()
                append("Proyecto activo: $it. Ruta del proyecto en el workspace: '$it' (usa esa ruta relativa en las herramientas).")
            }
        }.toString()
    }

    /** Bucle de conversación con herramientas para un mensaje del usuario. */
    suspend fun converse(
        agent: CatAgent,
        projectPath: String?,
        userMessage: String,
        history: List<Pair<Boolean, String>>
    ): AgentTurn {
        val contents = mutableListOf<Content>()
        history.takeLast(12).forEach { (isUser, text) ->
            if (text.isNotBlank()) contents.add(Content(if (isUser) "user" else "model", listOf(ChatPart.Text(text))))
        }
        contents.add(Content("user", listOf(ChatPart.Text(userMessage))))

        var toolExecutions = 0
        var lastError: String? = null
        val brain = brainFor(agent)

        repeat(MAX_ITERATIONS) { iteration ->
            when (val response = brain.provider.chat(
                ChatRequest(
                    model = brain.model.ifBlank { brain.provider.defaultModel },
                    systemPrompt = rolePrompt(agent, projectPath),
                    contents = contents.toList(),
                    tools = gateway.declarations(),
                    maxOutputTokens = MAX_TOKENS
                )
            )) {
                is ChatResponse.Error -> {
                    lastError = response.reason
                    return AgentTurn(
                        text = "No pude completar la respuesta: ${response.reason}",
                        toolExecutions = toolExecutions,
                        ok = false,
                        errorReason = response.reason
                    )
                }
                is ChatResponse.Ok -> {
                    val calls = response.parts.filterIsInstance<ChatPart.FunctionCall>()
                    if (calls.isEmpty() || iteration == MAX_ITERATIONS - 1) {
                        val text = response.parts.filterIsInstance<ChatPart.Text>().joinToString("\n") { it.text }
                        return AgentTurn(
                            text = text.ifBlank {
                                if (calls.isNotEmpty()) "Trabajo detenido por límite de iteraciones; el usuario puede pedir continuidad."
                                else "(el modelo no devolvió texto)"
                            },
                            toolExecutions = toolExecutions,
                            ok = true
                        )
                    }
                    contents.add(Content("model", response.parts))
                    val responseParts = mutableListOf<ChatPart>()
                    calls.forEach { call ->
                        val args = JsonArgs(call.argumentsJson)
                        listener.onToolExecution(agent.name, call.name, call.argumentsJson.take(300))
                        val result = gateway.execute(
                            agentName = agent.name,
                            projectPath = projectPath,
                            call = ToolCall(call.name, call.argumentsJson)
                        )
                        toolExecutions++
                        listener.onToolExecution(agent.name, call.name, result.detail.take(300))
                        val responseJson = com.squareup.moshi.Moshi.Builder()
                            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                            .build()
                            .adapter(Map::class.java)
                            .toJson(mapOf("ok" to result.ok, "detail" to result.detail.take(8000)))
                        responseParts.add(ChatPart.FunctionResponse(call.name, responseJson))
                        // El contexto de argumentos ayuda al modelo a corregir llamadas con parámetros faltantes
                        if (!result.ok && args.string("path") == null && call.name in setOf("read_file", "write_file", "delete_file", "list_directory")) {
                            responseParts.add(
                                ChatPart.Text("Nota: la llamada a ${call.name} falló. Verifica la ruta relativa con inspect_project e inténtalo de nuevo.")
                            )
                        }
                    }
                    contents.add(Content("user", responseParts))
                }
            }
        }
        return AgentTurn("(sin respuesta tras $MAX_ITERATIONS iteraciones)", toolExecutions, ok = false, errorReason = lastError)
    }
}
