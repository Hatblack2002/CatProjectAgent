package com.example.workspace

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.data.WorkspaceRepository
import com.example.model.CatFileItem
import com.example.data.formatRelativeActivity
import java.util.Locale

/**
 * Workspace del usuario sobre Storage Access Framework: la carpeta que el
 * usuario concede con ACTION_OPEN_DOCUMENT_TREE persiste entre reinicios,
 * los archivos creados son reales y visibles desde cualquier explorador.
 *
 * Mientras el usuario no conceda una carpeta, el workspace operativo es el
 * directorio privado de la app (archivos reales también), de modo que los
 * agentes nunca trabajan sobre datos ficticios.
 */
object WorkspaceManager {

    private const val PREFS = "cat_workspace_prefs"
    private const val KEY_TREE_URI = "tree_uri"
    private const val TREE_NAME = "CatProjectAgent"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun treeUri(context: Context): Uri? =
        prefs(context).getString(KEY_TREE_URI, null)?.let(Uri::parse)

    fun hasUserWorkspace(context: Context): Boolean {
        val uri = treeUri(context) ?: return false
        val flags = context.contentResolver.persistedUriPermissions
            .firstOrNull { it.uri == uri } ?: return false
        return flags.isReadPermission && flags.isWritePermission
    }

    fun saveTree(context: Context, uri: Uri): Boolean = try {
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
            android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flags)
        prefs(context).edit().putString(KEY_TREE_URI, uri.toString()).apply()
        ensureAppFolder(context)
        true
    } catch (_: SecurityException) {
        false
    }

    fun clearTree(context: Context) {
        val uri = treeUri(context) ?: return
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }
        prefs(context).edit().remove(KEY_TREE_URI).apply()
    }

    fun root(context: Context): DocumentFile? =
        if (hasUserWorkspace(context)) DocumentFile.fromTreeUri(context, treeUri(context)!!) else null

    /** Carpeta visible de la app dentro del árbol concedido (se crea si falta). */
    fun appFolder(context: Context): DocumentFile? {
        val tree = root(context) ?: return null
        val existing = tree.findFile(TREE_NAME)
        return existing ?: tree.createDirectory(TREE_NAME)
    }

    private fun ensureAppFolder(context: Context) {
        try {
            appFolder(context)
        } catch (_: Throwable) {
        }
    }

    fun workspaceLabel(context: Context): String =
        if (hasUserWorkspace(context)) {
            "${treeUri(context)!!.lastPathSegment?.substringAfter(':').orEmpty()}/$TREE_NAME"
        } else {
            "workspace privado de la app"
        }

    /** Lista el workspace operativo (SAF o privado) para la pestaña Archivos. */
    fun listForUser(context: Context): List<CatFileItem> {
        val folder = appFolder(context)
        return if (folder != null) {
            folder.listFiles()
                .sortedWith(
                    compareByDescending<DocumentFile> { it.isDirectory }
                        .thenBy { it.name?.lowercase(Locale.getDefault()) }
                )
                .map { entry ->
                    CatFileItem(
                        id = entry.uri.toString(),
                        name = entry.name.orEmpty(),
                        path = entry.uri.toString(),
                        isDirectory = entry.isDirectory,
                        itemCount = if (entry.isDirectory) entry.listFiles().size else null,
                        size = if (entry.isDirectory) null else WorkspaceRepository.formatSize(entry.length()),
                        lastModified = formatRelativeActivity(entry.lastModified()),
                        extension = entry.name?.substringAfterLast('.', "")?.lowercase(Locale.getDefault()) ?: ""
                    )
                }
        } else {
            WorkspaceRepository.list(context)
        }
    }

    /**
     * Localiza un DocumentFile a partir de una ruta relativa al workspace
     * operativo ("proyectos/demo/README.md"). Si [createParents] es true crea
     * los directorios intermedios que falten.
     */
    fun resolve(context: Context, relativePath: String, createParents: Boolean = false): DocumentFile? {
        val clean = relativePath.trim().trimStart('/')
        if (clean.isEmpty() || clean.contains("..")) return null
        val parts = clean.split('/').filter { it.isNotBlank() }
        var dir = appFolder(context)
            ?: WorkspaceRepository.privateFallbackRoot(context)?.let { DocumentFile.fromFile(it) }
            ?: return null
        for (i in parts.indices) {
            val name = parts[i]
            val isLeaf = i == parts.lastIndex
            val child = dir.findFile(name)
            when {
                child != null -> dir = child
                isLeaf -> return null
                createParents -> dir = dir.createDirectory(name) ?: return null
                else -> return null
            }
        }
        return dir
    }

    fun readBytes(context: Context, relativePath: String): ByteArray? {
        val doc = resolve(context, relativePath) ?: return null
        if (doc.isDirectory) return null
        return try {
            context.contentResolver.openInputStream(doc.uri)?.use { it.readBytes() }
        } catch (_: Throwable) {
            null
        }
    }

    fun writeBytes(
        context: Context,
        relativePath: String,
        bytes: ByteArray,
        mime: String = "application/octet-stream"
    ): Boolean {
        val clean = relativePath.trim().trimStart('/')
        if (clean.isEmpty() || clean.contains("..")) return false
        val parts = clean.split('/').filter { it.isNotBlank() }
        val parents = parts.dropLast(1).joinToString("/")
        val parent = if (parents.isEmpty()) {
            appFolder(context)
                ?: WorkspaceRepository.privateFallbackRoot(context)?.let { DocumentFile.fromFile(it) }
        } else {
            resolve(context, parents, createParents = true)
                ?: WorkspaceRepository.privateFallbackRoot(context)?.let {
                    val dir = java.io.File(it, parents)
                    dir.mkdirs()
                    DocumentFile.fromFile(dir)
                }
        } ?: return false
        val name = parts.last()
        val existing = parent.findFile(name)
        return try {
            if (existing != null && existing.isDirectory) return false
            val doc = existing ?: parent.createFile(mime, name) ?: return false
            context.contentResolver.openOutputStream(doc.uri, "wt")?.use { it.write(bytes) } != null
        } catch (_: Throwable) {
            false
        }
    }

    fun createDirectory(context: Context, relativePath: String): Boolean {
        val clean = relativePath.trim().trimStart('/').trimEnd('/')
        if (clean.isEmpty() || clean.contains("..")) return false
        return resolve(context, clean, createParents = true)?.isDirectory == true
    }

    fun delete(context: Context, relativePath: String): Boolean =
        resolve(context, relativePath)?.delete() == true

    fun rename(context: Context, relativePath: String, newName: String): Boolean {
        if (newName.isBlank() || newName.contains('/')) return false
        return resolve(context, relativePath)?.renameTo(newName.trim()) == true
    }

    fun move(context: Context, fromPath: String, toPath: String): Boolean {
        val doc = resolve(context, fromPath) ?: return false
        if (doc.isDirectory) {
            val created = createDirectory(context, toPath)
            doc.listFiles().forEach { child ->
                if (!move(context, "$fromPath/${child.name}", "$toPath/${child.name}")) return false
            }
            return created && doc.delete()
        }
        val bytes = readBytes(context, fromPath) ?: return false
        val mime = doc.type ?: "application/octet-stream"
        return writeBytes(context, toPath, bytes, mime) && doc.delete()
    }

    fun search(context: Context, query: String, maxResults: Int = 50): List<CatFileItem> {
        val needle = query.trim().lowercase(Locale.getDefault())
        if (needle.isEmpty()) return emptyList()
        val results = mutableListOf<CatFileItem>()
        fun walk(dir: DocumentFile, prefix: String, depth: Int) {
            if (results.size >= maxResults || depth > 8) return
            dir.listFiles().forEach { entry ->
                val rel = if (prefix.isEmpty()) entry.name.orEmpty() else "$prefix/${entry.name}"
                if (entry.name?.lowercase(Locale.getDefault())?.contains(needle) == true) {
                    results.add(
                        CatFileItem(
                            id = entry.uri.toString(),
                            name = entry.name.orEmpty(),
                            path = rel,
                            isDirectory = entry.isDirectory,
                            itemCount = if (entry.isDirectory) entry.listFiles().size else null,
                            size = if (entry.isDirectory) null else WorkspaceRepository.formatSize(entry.length()),
                            lastModified = formatRelativeActivity(entry.lastModified()),
                            extension = entry.name?.substringAfterLast('.', "") ?: ""
                        )
                    )
                }
                if (entry.isDirectory) walk(entry, rel, depth + 1)
            }
        }
        val rootDoc = appFolder(context)
            ?: WorkspaceRepository.privateFallbackRoot(context)?.let { DocumentFile.fromFile(it) }
            ?: return emptyList()
        walk(rootDoc, "", 0)
        return results
    }

    /** Resumen real del árbol para la herramienta inspect_project. */
    fun treeSummary(context: Context, maxEntries: Int = 400): String {
        val sb = StringBuilder()
        var count = 0
        fun walk(dir: DocumentFile, indent: String, depth: Int) {
            if (count >= maxEntries || depth > 6) return
            dir.listFiles()
                .sortedWith(compareByDescending<DocumentFile> { it.isDirectory }.thenBy { it.name })
                .forEach { entry ->
                    if (count >= maxEntries) return
                    count++
                    if (entry.isDirectory) {
                        sb.appendLine("$indent${entry.name}/")
                        walk(entry, "$indent  ", depth + 1)
                    } else {
                        sb.appendLine("$indent${entry.name} (${WorkspaceRepository.formatSize(entry.length())})")
                    }
                }
        }
        val rootDoc = appFolder(context)
            ?: WorkspaceRepository.privateFallbackRoot(context)?.let { DocumentFile.fromFile(it) }
        if (rootDoc == null) {
            sb.appendLine("(workspace no disponible)")
        } else {
            walk(rootDoc, "", 0)
            if (count >= maxEntries) sb.appendLine("… (resumen truncado a $maxEntries entradas)")
        }
        return sb.toString().trim()
    }

    /** Detecta el sistema de build del proyecto por sus archivos reales. */
    fun detectBuildSystem(context: Context, projectPath: String): String? {
        val projectDoc = resolve(context, projectPath) ?: return null
        if (!projectDoc.isDirectory) return null
        val names = projectDoc.listFiles().mapNotNull { it.name }
        return when {
            names.any { it == "settings.gradle.kts" || it == "settings.gradle" } -> "android-gradle"
            names.any { it == "build.gradle.kts" || it == "build.gradle" } -> "gradle"
            names.any { it == "requirements.txt" || it == "pyproject.toml" } -> "python"
            names.any { it == "package.json" } -> "node"
            names.any { it == "Makefile" } -> "make"
            else -> null
        }
    }

    /** Copia un archivo del workspace SAF a un destino del filesystem host (puente hacia el entorno Linux). */
    fun exportToHost(context: Context, relativePath: String, target: java.io.File): Boolean {
        val bytes = readBytes(context, relativePath) ?: return false
        return try {
            target.parentFile?.mkdirs()
            target.writeBytes(bytes)
            true
        } catch (_: Throwable) {
            false
        }
    }

    /** Importa un archivo del filesystem host al workspace SAF (artefactos generados en Linux). */
    fun importFromHost(context: Context, source: java.io.File, relativePath: String): Boolean {
        if (!source.isFile) return false
        return writeBytes(context, relativePath, source.readBytes())
    }
}
