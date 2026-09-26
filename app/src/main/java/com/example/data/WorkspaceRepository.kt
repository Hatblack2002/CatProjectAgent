package com.example.data

import android.content.Context
import com.example.model.CatFileItem
import java.io.File
import java.util.Locale

/**
 * Workspace real de la app: un directorio en el almacenamiento privado que
 * también se bindea dentro del entorno Linux en [GUEST_PATH]. Los archivos que
 * el usuario ve en la app son exactamente los que los agentes y la terminal
 * manipulan, y viceversa.
 */
object WorkspaceRepository {

    const val GUEST_PATH = "/root/workspace"

    private val CATEGORY_DIRS = listOf("Documentos", "Imágenes", "Código", "Modelos", "Recursos")

    fun hostDir(context: Context): File = File(context.filesDir, "workspace")

    fun projectDir(context: Context, projectId: String): File =
        File(File(hostDir(context), "projects"), projectId)

    fun ensureBase(context: Context): File {
        val root = hostDir(context)
        root.mkdirs()
        File(root, "projects").mkdirs()
        CATEGORY_DIRS.forEach { File(root, it).mkdirs() }
        return root
    }

    fun createProjectDir(context: Context, projectId: String): File =
        projectDir(context, projectId).apply { mkdirs() }

    fun list(context: Context): List<CatFileItem> {
        val root = ensureBase(context)
        val entries = root.listFiles()
            ?.sortedWith(
                compareByDescending<File> { it.isDirectory }
                    .thenBy { it.name.lowercase(Locale.getDefault()) }
            )
            ?: emptyList()
        return entries.map { entry ->
            CatFileItem(
                id = entry.absolutePath,
                name = entry.name,
                path = entry.absolutePath,
                isDirectory = entry.isDirectory,
                itemCount = if (entry.isDirectory) entry.listFiles()?.size ?: 0 else null,
                size = if (entry.isDirectory) null else formatSize(entry.length()),
                lastModified = formatRelativeActivity(entry.lastModified()),
                extension = if (entry.isDirectory) "" else entry.extension.lowercase(Locale.getDefault())
            )
        }
    }

    fun countProjectFiles(context: Context, projectId: String): Int =
        projectDir(context, projectId).walkTopDown().count { it.isFile }

    fun formatSize(bytes: Long): String = when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024 -> String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024 * 1024 -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024))
        else -> String.format(Locale.getDefault(), "%.1f GB", bytes / (1024.0 * 1024 * 1024))
    }
}
