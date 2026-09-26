package com.example.ai

/**
 * Resolución de la clave de API de Gemini.
 *
 * El Secrets Gradle Plugin solo genera BuildConfig.GEMINI_API_KEY cuando la
 * clave está definida en el archivo .env del proyecto, por eso la lectura se
 * hace por reflexión: así el proyecto compila con o sin la clave configurada.
 */
object AiKeyProvider {

    private const val BUILD_CONFIG_CLASS = "com.example.BuildConfig"
    private const val KEY_FIELD = "GEMINI_API_KEY"

    fun apiKey(): String? =
        readBuildConfigKey()?.takeIf { it.isNotBlank() && !it.contains("MY_GEMINI", ignoreCase = true) }

    private fun readBuildConfigKey(): String? = try {
        val field = Class.forName(BUILD_CONFIG_CLASS).getDeclaredField(KEY_FIELD)
        field.isAccessible = true
        field.get(null) as? String
    } catch (_: Throwable) {
        null
    }
}
