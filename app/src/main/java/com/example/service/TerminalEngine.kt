package com.example.service

import android.content.Context
import com.example.model.LineType
import com.example.model.TerminalLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Motor de comandos: todo se envía tal cual al PTY real (PRoot + rootfs
 * Ubuntu); no existe ningún dispatcher que fabrique salidas.
 */
object TerminalEngine {

    fun createInitialSessionLines(context: Context): List<TerminalLine> = listOf(
        TerminalLine("[CatProjectAgent] Motor: PTY nativo (termux-app GPLv3) + PRoot + rootfs Ubuntu 24.04", LineType.SYSTEM),
        TerminalLine("[CatProjectAgent] Preparando el entorno real: descarga, SHA256, extracción y verificación.", LineType.SYSTEM)
    )

    suspend fun executeCommand(
        command: String,
        currentDir: String,
        context: Context,
        sessionId: String = "term-1"
    ): CommandResult = withContext(Dispatchers.IO) {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) {
            return@withContext CommandResult(emptyList(), currentDir)
        }

        val shouldClear = trimmed == "clear"

        val lines = PtyBridge.runCommand(sessionId, trimmed, context)
            ?: listOf(
                TerminalLine(
                    "[motor] Sesión no disponible. Causa: ${PtyBridge.lastFailureReason(sessionId)}. Estado del entorno: ${describe(LinuxBootstrap.status.value)}",
                    LineType.ERROR
                )
            )

        CommandResult(lines, currentDir, shouldClear)
    }

    fun describe(status: LinuxBootstrap.Status): String = when (status) {
        is LinuxBootstrap.Status.Downloading ->
            "descargando rootfs ${status.downloadedBytes / (1024 * 1024)} MB de ${if (status.totalBytes > 0) "${status.totalBytes / (1024 * 1024)} MB" else "?"}"
        is LinuxBootstrap.Status.VerifyingHash -> "verificando SHA256"
        is LinuxBootstrap.Status.Extracting -> "extrayendo (${status.processed} entradas)"
        is LinuxBootstrap.Status.Hardening -> "configurando rootfs"
        is LinuxBootstrap.Status.Checking -> "verificando rootfs"
        is LinuxBootstrap.Status.Ready -> "rootfs listo (${status.info.osPrettyName}, ${status.info.dpkgPackages} paquetes dpkg)"
        is LinuxBootstrap.Status.Failed -> "FALLÓ: ${status.reason}"
        LinuxBootstrap.Status.NotStarted -> "sin iniciar"
    }
}

data class CommandResult(
    val lines: List<TerminalLine>,
    val newDir: String,
    val shouldClear: Boolean = false
)
