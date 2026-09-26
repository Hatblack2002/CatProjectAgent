package com.example.engine

import android.content.Context
import com.example.model.LineType
import com.example.model.TerminalLine
import com.example.service.LinuxBootstrap
import com.example.service.PtyBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Motor de ejecución interno de CatProjectAgent: convierte peticiones de
 * herramientas (run_process, build_project, run_tests, install_dependency) en
 * comandos reales dentro del entorno Linux (PTY nativo + PRoot + Ubuntu).
 * No expone ninguna terminal al usuario: su única salida es un ExecResult.
 */
object ExecutionEngine {

    private const val ENGINE_SESSION_ID = "agent-shell"
    private const val EXIT_MARKER = "__CPA_EXIT__"

    data class ExecResult(
        val command: String,
        val exitCode: Int?,
        val stdout: String,
        val lines: List<String>,
        val timedOut: Boolean,
        val bootstrapped: Boolean
    ) {
        val success: Boolean get() = exitCode == 0
        fun tail(maxLines: Int = 120): String =
            lines.takeLast(maxLines).joinToString("\n").ifBlank { "(sin salida)" }
    }

    /** Garantiza el entorno (descarga/verificación bajo demanda) y lo informa. */
    suspend fun ensureEnvironment(context: Context): Pair<Boolean, String> {
        if (PtyBridge.isEngineReady(context)) return true to "listo"
        var bootstrapped = false
        LinuxBootstrap.ensure(context).let { status ->
            if (status is LinuxBootstrap.Status.Ready) bootstrapped = true
            if (status is LinuxBootstrap.Status.Failed) {
                return false to "el entorno Linux no pudo instalarse: ${status.reason}"
            }
        }
        if (!PtyBridge.isEngineReady(context)) return false to "el entorno Linux no quedó operativo"
        return true to if (bootstrapped) "instalado ahora" else "listo"
    }

    /**
     * Ejecuta un comando real y captura su salida y código de salida. El
     * marcador de salida se añade a la propia línea de comandos, de modo que
     * el transcript del PTY contiene el estado de terminación exacto.
     */
    suspend fun run(
        context: Context,
        command: String,
        cwd: String = "~",
        timeoutMs: Long = 120_000L
    ): ExecResult = withContext(Dispatchers.IO) {
        val (ready, message) = ensureEnvironment(context)
        if (!ready) {
            return@withContext ExecResult(
                command = command,
                exitCode = null,
                stdout = message,
                lines = listOf(message),
                timedOut = false,
                bootstrapped = false
            )
        }
        val effective = if (cwd != "~" && cwd.isNotBlank()) "cd '$cwd' 2>/dev/null || cd ~; $command" else command
        val wrapped = "$effective; echo \"$EXIT_MARKER:\$?\""
        val terminalLines = PtyBridge.runCommand(ENGINE_SESSION_ID, wrapped, context, timeoutMs)
            ?: return@withContext ExecResult(
                command = command,
                exitCode = null,
                stdout = "sesión no disponible: ${PtyBridge.lastFailureReason(ENGINE_SESSION_ID)}",
                lines = listOf("sesión no disponible: ${PtyBridge.lastFailureReason(ENGINE_SESSION_ID)}"),
                timedOut = false,
                bootstrapped = false
            )

        val texts = terminalLines.map { it.text }
        val markerLine = texts.lastOrNull { it.startsWith("$EXIT_MARKER:") }
        val exitCode = markerLine?.substringAfter("$EXIT_MARKER:")?.trim()?.toIntOrNull()
        val body = texts
            .dropWhile { it.trim() == wrapped.trim() || it.trim() == command.trim() }
            .filterNot { it.startsWith("$EXIT_MARKER:") }
            .dropLastWhile { it.trimEnd().endsWith("$") || it.trimEnd().endsWith("#") }
        val timedOut = exitCode == null && terminalLines.any { it.type == LineType.SYSTEM }
        ExecResult(
            command = command,
            exitCode = exitCode,
            stdout = body.joinToString("\n").trim(),
            lines = body,
            timedOut = timedOut,
            bootstrapped = true
        )
    }

    /** Descripción legible del estado del entorno para la UI. */
    fun describe(status: LinuxBootstrap.Status): String = when (status) {
        is LinuxBootstrap.Status.Downloading ->
            "descargando rootfs ${status.downloadedBytes / (1024 * 1024)} MB de ${if (status.totalBytes > 0) "${status.totalBytes / (1024 * 1024)} MB" else "?"}"
        is LinuxBootstrap.Status.VerifyingHash -> "verificando SHA256"
        is LinuxBootstrap.Status.Extracting -> "extrayendo (${status.processed} entradas)"
        is LinuxBootstrap.Status.Hardening -> "configurando rootfs"
        is LinuxBootstrap.Status.Checking -> "verificando rootfs"
        is LinuxBootstrap.Status.Ready -> "listo (${status.info.osPrettyName}, ${status.info.dpkgPackages} paquetes)"
        is LinuxBootstrap.Status.Failed -> "FALLÓ: ${status.reason}"
        LinuxBootstrap.Status.NotStarted -> "sin iniciar"
    }

    fun terminalLinesForDiagnostics(context: Context): List<TerminalLine> = listOf(
        TerminalLine("[CatProjectAgent] Motor: PTY nativo + PRoot + Ubuntu 24.04", LineType.SYSTEM)
    )
}
