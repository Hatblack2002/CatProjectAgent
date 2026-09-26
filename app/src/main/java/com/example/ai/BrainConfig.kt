package com.example.ai

import android.content.Context
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Slots de cerebro: cada uno puede tener su propio proveedor y su propia
 * clave de API. El slot PRINCIPAL es el cerebro por defecto que atiende a
 * todos los agentes; los slots de agente permiten sustituir el cerebro de
 * un rol concreto (Arquitecto, Diseñador, Programador, Analista) por otro
 * proveedor distinto.
 */
object BrainSlots {
    const val PRINCIPAL = "principal"
    const val ARCHITECT = "agent_architect"
    const val DESIGNER = "agent_designer"
    const val CODER = "agent_coder"
    const val ANALYST = "agent_analyst"

    val AGENT_SLOTS = listOf(ARCHITECT, DESIGNER, CODER, ANALYST)
    val ALL = listOf(PRINCIPAL) + AGENT_SLOTS
}

@JsonClass(generateAdapter = false)
data class BrainSlotConfig(
    val providerId: String = GeminiProvider.ID,
    val apiKey: String = "",
    val model: String = ""
) {
    fun hasKey() = apiKey.isNotBlank()
}

/**
 * Persistencia real de la configuración de cerebros en el almacenamiento
 * privado de la app. Solo guarda configuración introducida por el usuario:
 * sin claves guardadas la base queda vacía y el resolver cae a la clave
 * compilada de Gemini (si existe) como último recurso.
 */
object ProviderKeys {

    private const val PREFS_FILE = "ai_brain_configs"

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BrainSlotConfig::class.java)

    fun load(context: Context, slot: String): BrainSlotConfig? {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val raw = prefs.getString(slot, null) ?: return null
        return try {
            adapter.fromJson(raw)
        } catch (_: Throwable) {
            null
        }
    }

    fun save(context: Context, slot: String, config: BrainSlotConfig) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        prefs.edit().putString(slot, adapter.toJson(config)).apply()
    }

    fun clear(context: Context, slot: String) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        prefs.edit().remove(slot).apply()
    }
}
