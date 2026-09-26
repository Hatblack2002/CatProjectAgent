package com.example.workspace

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.example.data.WorkspaceRepository
import java.io.File

/**
 * Puente entre el workspace del usuario (SAF) y el mirror host que el entorno
 * Linux bindea en /root/mirror. Antes de ejecutar comandos se empujan los
 * archivos del proyecto al mirror; después se importan de vuelta los
 * artefactos generados (APKs, binarios, logs) para que el usuario los vea en
 * su explorador. Nunca borra archivos del lado del usuario.
 */
object WorkspaceBridge {

    private const val SYNC_MARKER = ".cpa_sync"

    private fun mirrorRoot(context: Context): File =
        File(WorkspaceRepository.hostDir(context), "mirror")

    fun mirrorDirFor(context: Context, projectPath: String): File =
        File(mirrorRoot(context), projectPath.trim('/'))

    /** Ruta del mirror del proyecto dentro del entorno Linux. */
    fun guestPathFor(projectPath: String): String =
        "/root/mirror/${projectPath.trim('/')}"

    /**
     * Empuja el proyecto del workspace del usuario al mirror de ejecución.
     * Devuelve el número de archivos copiados.
     */
    fun push(context: Context, projectPath: String): Int {
        val source = WorkspaceManager.resolve(context, projectPath) ?: return 0
        if (!source.isDirectory) return 0
        val targetRoot = mirrorDirFor(context, projectPath)
        var copied = 0
        pushTree(context, source, projectPath, targetRoot, "") { copied++ }
        File(targetRoot, SYNC_MARKER).writeText(System.currentTimeMillis().toString())
        return copied
    }

    /**
     * Importa del mirror al workspace del usuario los archivos nuevos o
     * modificados desde [sinceMillis] (artefactos de compilación incluidos).
     * Devuelve la lista de rutas relativas dentro del proyecto.
     */
    fun pull(context: Context, projectPath: String, sinceMillis: Long): List<String> {
        val projectMirror = mirrorDirFor(context, projectPath)
        if (!projectMirror.isDirectory) return emptyList()
        val imported = mutableListOf<String>()
        pullTree(context, projectMirror, projectPath, "", sinceMillis, imported)
        return imported
    }

    private fun pushTree(
        context: Context,
        dir: DocumentFile,
        projectPath: String,
        targetRoot: File,
        prefix: String,
        onFile: () -> Unit
    ) {
        dir.listFiles().forEach { entry ->
            val name = entry.name ?: return@forEach
            val rel = if (prefix.isEmpty()) name else "$prefix/$name"
            if (entry.isDirectory) {
                pushTree(context, entry, projectPath, targetRoot, rel, onFile)
            } else {
                if (name == SYNC_MARKER) return@forEach
                if (WorkspaceManager.exportToHost(context, "$projectPath/$rel", File(targetRoot, rel))) {
                    onFile()
                }
            }
        }
    }

    private fun pullTree(
        context: Context,
        dir: File,
        projectPath: String,
        prefix: String,
        sinceMillis: Long,
        imported: MutableList<String>
    ) {
        dir.listFiles()?.forEach { file ->
            if (file.name == SYNC_MARKER) return@forEach
            val rel = if (prefix.isEmpty()) file.name else "$prefix/${file.name}"
            if (file.isDirectory) {
                pullTree(context, file, projectPath, rel, sinceMillis, imported)
            } else if (file.lastModified() >= sinceMillis) {
                if (WorkspaceManager.importFromHost(context, file, "$projectPath/$rel")) {
                    imported.add(rel)
                }
            }
        }
    }
}
