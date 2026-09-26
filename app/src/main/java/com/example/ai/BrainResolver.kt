package com.example.ai

import android.content.Context

/**
 * Cerebro resuelto para un agente: el proveedor real con su clave, el
 * modelo que debe usar y de dónde salió la decisión (cerebro propio del
 * agente, cerebro principal compartido o clave compilada de Gemini).
 */
data class ResolvedBrain(
    val provider: AIProvider,
    val model: String,
    val sourceLabel: String,
    val providerId: String
)

/**
 * Resolución de cerebros por agente. Orden de decisión:
 *
 * 1. Slot propio del agente (p. ej. agent_designer): si el usuario guardó
 *    una clave para ese rol, ese cerebro atiende a ese agente.
 * 2. Slot PRINCIPAL: si el usuario guardó una clave general, ese cerebro
 *    atiende a todos los agentes que no tengan uno propio.
 * 3. GEMINI_API_KEY compilada en el .env del proyecto: cerebro por defecto
 *    para que una instalación nueva funcione sin configurar nada.
 *
 * Un solo cerebro puede ocupar el puesto de todos; cada agente puede
 * tener además uno distinto.
 */
class BrainResolver(private val context: Context) {

    /** Configuración guardada de un slot (sin resolver). */
    fun configFor(slot: String): BrainSlotConfig? = ProviderKeys.load(context, slot)

    /** Resuelve el cerebro real que atiende a un agente concreto. */
    fun resolve(agentId: String, agentModel: String = ""): ResolvedBrain {
        val own = ProviderKeys.load(context, agentId)
        if (own?.hasKey() == true) {
            return brainFrom(own, sourceLabel = "cerebro propio")
        }
        val principal = ProviderKeys.load(context, BrainSlots.PRINCIPAL)
        if (principal?.hasKey() == true) {
            return brainFrom(principal, sourceLabel = "cerebro principal")
        }
        val compiled = GeminiProvider(context, null)
        return ResolvedBrain(
            provider = compiled,
            model = agentModel.ifBlank { compiled.defaultModel },
            sourceLabel = "cerebro por defecto (GEMINI_API_KEY del .env)",
            providerId = GeminiProvider.ID
        )
    }

    /** True si existe algún cerebro utilizable: claves guardadas o compilada. */
    fun isAnyConfigured(): Boolean =
        BrainSlots.ALL.any { ProviderKeys.load(context, it)?.hasKey() == true } ||
            AiKeyProvider.apiKey() != null

    /** Resumen real del estado de cerebros, para Ajustes y el setup. */
    fun summary(): String {
        val principal = ProviderKeys.load(context, BrainSlots.PRINCIPAL)
        val ownCount = BrainSlots.AGENT_SLOTS.count { ProviderKeys.load(context, it)?.hasKey() == true }
        val parts = mutableListOf<String>()
        parts.add(
            if (principal?.hasKey() == true) {
                "principal: ${labelFor(principal)}"
            } else if (AiKeyProvider.apiKey() != null) {
                "principal: Gemini del .env (sin clave guardada)"
            } else {
                "principal: sin clave"
            }
        )
        if (ownCount > 0) parts.add("$ownCount agente(s) con cerebro propio")
        return parts.joinToString(" · ")
    }

    /** Proveedor+modelo de una config guardada, para pruebas y visualización. */
    fun brainFrom(config: BrainSlotConfig, sourceLabel: String): ResolvedBrain {
        val provider = ProviderRegistry.create(context, config.providerId, config.apiKey)
        return ResolvedBrain(
            provider = provider,
            model = config.model.ifBlank { provider.defaultModel },
            sourceLabel = sourceLabel,
            providerId = config.providerId
        )
    }

    /** Prueba real de conexión con valores de borrador (aún sin guardar). */
    suspend fun testDraft(providerId: String, apiKey: String, model: String): ProviderTestResult {
        if (apiKey.isBlank()) {
            return ProviderTestResult(false, "escribe una clave de API antes de probar")
        }
        val provider = ProviderRegistry.create(context, providerId, apiKey)
        return provider.testConnection()
    }

    /** Prueba real del cerebro por defecto (slot principal o Gemini compilado). */
    suspend fun testPrincipal(): ProviderTestResult {
        val principal = ProviderKeys.load(context, BrainSlots.PRINCIPAL)
        if (principal?.hasKey() == true) {
            return brainFrom(principal, "cerebro principal").provider.testConnection()
        }
        if (AiKeyProvider.apiKey() != null) {
            return GeminiProvider(context, null).testConnection()
        }
        return ProviderTestResult(false, "sin clave de API: introdúcela en Ajustes → Cerebros de IA")
    }

    private fun labelFor(config: BrainSlotConfig): String {
        val descriptor = ProviderRegistry.descriptor(config.providerId)
        val model = config.model.ifBlank { descriptor?.defaultModel ?: "" }
        return "${descriptor?.displayName ?: config.providerId} · $model"
    }
}
