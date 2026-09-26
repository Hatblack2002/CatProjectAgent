package com.example.tools

import android.content.Context
import com.example.data.BuildRecordDao
import com.example.data.BuildRecordEntity
import com.example.data.CatDatabase
import com.example.data.IssueDao
import com.example.data.IssueEntity
import com.example.data.IssueSeverity
import com.example.data.IssueStatus
import com.example.data.TaskDao
import com.example.data.ToolLogDao
import com.example.data.ToolLogEntity
import com.example.engine.ExecutionEngine
import com.example.model.TaskStatus as ModelTaskStatus
import com.example.workspace.WorkspaceBridge
import com.example.workspace.WorkspaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Gateway único de herramientas: recibe la solicitud del agente, verifica el
 * contexto y el nivel de riesgo, pide aprobación cuando corresponde, ejecuta
 * contra implementaciones reales (workspace SAF, entorno Linux), registra la
 * operación en Room y devuelve el resultado verificable al modelo.
 */
class ToolGateway(
    private val context: Context,
    private val approvalGate: ApprovalGate
) {

    interface ApprovalGate {
        /** Devuelve true solo si el usuario aprobó explícitamente la acción. */
        suspend fun requestApproval(request: ApprovalContext): Boolean
    }

    data class ApprovalContext(
        val agentName: String,
        val actionTitle: String,
        val reason: String,
        val affected: List<String>
    )

    enum class ToolRisk { SAFE, SENSITIVE, DANGEROUS }

    data class ToolSpec(
        val name: String,
        val description: String,
        val parametersSchema: String,
        val risk: ToolRisk
    )

    data class ToolCall(val name: String, val argumentsJson: String)

    data class ToolResult(val ok: Boolean, val detail: String, val dataJson: String? = null)

    private val toolLogDao: ToolLogDao = CatDatabase.get(context).toolLogDao()
    private val issueDao: IssueDao = CatDatabase.get(context).issueDao()
    private val buildDao: BuildRecordDao = CatDatabase.get(context).buildRecordDao()
    private val taskDao: TaskDao = CatDatabase.get(context).taskDao()

    val specs: List<ToolSpec> = listOf(
        ToolSpec(
            "inspect_project",
            "Obtiene el árbol de archivos real del workspace del usuario. Úsalo primero para conocer el estado del proyecto.",
            """{"type":"OBJECT","properties":{},"required":[]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "list_directory",
            "Lista el contenido real de un directorio del workspace (ruta relativa, ej: 'proyectos/demo').",
            """{"type":"OBJECT","properties":{"path":{"type":"STRING"}},"required":["path"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "read_file",
            "Lee el contenido real de un archivo de texto del workspace (máximo 64 KB).",
            """{"type":"OBJECT","properties":{"path":{"type":"STRING"}},"required":["path"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "write_file",
            "Crea o sobrescribe un archivo real en el workspace con el contenido indicado. Crea los directorios que falten. Para archivos nuevos o reescrituras completas.",
            """{"type":"OBJECT","properties":{"path":{"type":"STRING"},"content":{"type":"STRING"}},"required":["path","content"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "edit_file",
            "Edita un archivo existente con bloques SEARCH/REPLACE exactos (formato probado de Aider/OpenHands). Formato: <<<<<<< SEARCH luego el texto exacto actual, =======, el texto nuevo, >>>>>>> REPLACE. Varios bloques seguidos permitidos. El SEARCH debe coincidir literalmente con el archivo actual.",
            """{"type":"OBJECT","properties":{"path":{"type":"STRING"},"diff":{"type":"STRING"}},"required":["path","diff"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "delete_file",
            "Elimina un archivo o directorio del workspace. Requiere aprobación del usuario.",
            """{"type":"OBJECT","properties":{"path":{"type":"STRING"}},"required":["path"]}""",
            ToolRisk.DANGEROUS
        ),
        ToolSpec(
            "move_file",
            "Mueve o renombra un archivo dentro del workspace. Requiere aprobación del usuario.",
            """{"type":"OBJECT","properties":{"from":{"type":"STRING"},"to":{"type":"STRING"}},"required":["from","to"]}""",
            ToolRisk.SENSITIVE
        ),
        ToolSpec(
            "search_files",
            "Busca archivos por nombre en todo el workspace real.",
            """{"type":"OBJECT","properties":{"query":{"type":"STRING"}},"required":["query"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "run_process",
            "Ejecuta un comando real en el entorno Linux interno (Ubuntu 24.04 con PRoot). La salida real se devuelve. Requiere aprobación.",
            """{"type":"OBJECT","properties":{"command":{"type":"STRING"},"timeout_sec":{"type":"INTEGER"}},"required":["command"]}""",
            ToolRisk.SENSITIVE
        ),
        ToolSpec(
            "install_dependency",
            "Instala una dependencia en el entorno Linux (apt-get, pip, gradle wrapper, etc.). Requiere aprobación del usuario.",
            """{"type":"OBJECT","properties":{"command":{"type":"STRING"}},"required":["command"]}""",
            ToolRisk.DANGEROUS
        ),
        ToolSpec(
            "build_project",
            "Compila el proyecto real detectando su sistema de build (Gradle/Make/Python) y registra el resultado del build. Requiere aprobación.",
            """{"type":"OBJECT","properties":{"task":{"type":"STRING"}},"required":[]}""",
            ToolRisk.SENSITIVE
        ),
        ToolSpec(
            "run_tests",
            "Ejecuta las pruebas reales del proyecto en el entorno Linux. Requiere aprobación.",
            """{"type":"OBJECT","properties":{"args":{"type":"STRING"}},"required":[]}""",
            ToolRisk.SENSITIVE
        ),
        ToolSpec(
            "create_task",
            "Crea una tarea real asociada al proyecto actual (planificación del trabajo).",
            """{"type":"OBJECT","properties":{"title":{"type":"STRING"},"assigned_agent":{"type":"STRING"}},"required":["title"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "complete_task",
            "Marca una tarea existente del proyecto como completada, solo si hay evidencia real.",
            """{"type":"OBJECT","properties":{"task_id":{"type":"STRING"}},"required":["task_id"]}""",
            ToolRisk.SAFE
        ),
        ToolSpec(
            "report_issue",
            "Registra un problema verificable detectado durante la revisión, con severidad, evidencia y reproducción.",
            """{"type":"OBJECT","properties":{"severity":{"type":"STRING"},"category":{"type":"STRING"},"requirement":{"type":"STRING"},"problem":{"type":"STRING"},"expected":{"type":"STRING"},"actual":{"type":"STRING"},"evidence":{"type":"STRING"},"file":{"type":"STRING"},"line":{"type":"INTEGER"},"reproduction":{"type":"STRING"}},"required":["severity","category","problem","expected","actual"]}""",
            ToolRisk.SAFE
        )
    )

    fun declarations() = specs.map { com.example.ai.ToolDeclaration(it.name, it.description, it.parametersSchema) }

    fun specByName(name: String): ToolSpec? = specs.firstOrNull { it.name == name }

    suspend fun execute(
        agentName: String,
        projectPath: String?,
        call: ToolCall
    ): ToolResult = withContext(Dispatchers.IO) {
        val started = System.currentTimeMillis()
        val spec = specByName(call.name)
        val result = if (spec == null) {
            ToolResult(false, "herramienta desconocida: ${call.name}")
        } else {
            runGuarded(agentName, projectPath, spec, call)
        }
        val elapsed = System.currentTimeMillis() - started
        toolLogDao.insert(
            ToolLogEntity(
                id = UUID.randomUUID().toString(),
                projectId = projectPath,
                agentName = agentName,
                tool = call.name,
                arguments = call.argumentsJson.take(2000),
                outcome = if (result.ok) "OK" else "ERROR",
                detail = result.detail.take(4000),
                durationMs = elapsed,
                timestampMillis = System.currentTimeMillis()
            )
        )
        result
    }

    private suspend fun runGuarded(
        agentName: String,
        projectPath: String?,
        spec: ToolSpec,
        call: ToolCall
    ): ToolResult {
        return try {
            if (spec.risk != ToolRisk.SAFE) {
                val approved = approvalGate.requestApproval(
                    ApprovalContext(
                        agentName = agentName,
                        actionTitle = approvalTitle(spec.name),
                        reason = "$agentName solicita ejecutar «${spec.name}». Se ejecutará de forma real en el dispositivo o en el entorno Linux.",
                        affected = affectedOf(call)
                    )
                )
                if (!approved) {
                    return ToolResult(false, "el usuario RECHAZÓ la ejecución de ${spec.name}. No insistas; pregunta qué quiere hacer.")
                }
            }
            dispatch(agentName, projectPath, spec, call)
        } catch (e: Throwable) {
            ToolResult(false, "error real ejecutando ${spec.name}: ${e.javaClass.simpleName}: ${e.message ?: "sin detalle"}")
        }
    }

    private suspend fun dispatch(
        agentName: String,
        projectPath: String?,
        spec: ToolSpec,
        call: ToolCall
    ): ToolResult {
        val args = JsonArgs(call.argumentsJson)
        return when (spec.name) {
            "inspect_project" -> ToolResult(
                true,
                "Árbol real del workspace (${WorkspaceManager.workspaceLabel(context)}):\n\n" +
                    WorkspaceManager.treeSummary(context).ifBlank { "(workspace vacío)" }
            )
            "list_directory" -> {
                val path = args.string("path") ?: return ToolResult(false, "falta 'path'")
                val listing = WorkspaceManager.resolve(context, path)?.takeIf { it.isDirectory }
                    ?.listFiles()
                    ?.joinToString("\n") { doc ->
                        if (doc.isDirectory) "[dir]  ${doc.name}" else "[file] ${doc.name} (${doc.length()} bytes)"
                    }
                if (listing == null) ToolResult(false, "el directorio '$path' no existe en el workspace real")
                else ToolResult(true, listing.ifBlank { "(directorio vacío)" })
            }
            "read_file" -> {
                val path = args.string("path") ?: return ToolResult(false, "falta 'path'")
                val bytes = WorkspaceManager.readBytes(context, path)
                    ?: return ToolResult(false, "el archivo '$path' no existe o no se pudo leer")
                if (bytes.size > 64 * 1024) {
                    ToolResult(true, String(bytes, 0, 64 * 1024, Charsets.UTF_8) + "\n… (truncado a 64 KB de ${bytes.size} bytes)")
                } else {
                    ToolResult(true, String(bytes, Charsets.UTF_8).ifBlank { "(archivo vacío)" })
                }
            }
            "write_file" -> {
                val path = args.string("path") ?: return ToolResult(false, "falta 'path'")
                val content = args.string("content") ?: return ToolResult(false, "falta 'content'")
                val ok = WorkspaceManager.writeBytes(context, path, content.toByteArray(Charsets.UTF_8))
                if (ok) ToolResult(true, "archivo escrito realmente: $path (${content.toByteArray().size} bytes)")
                else ToolResult(false, "no se pudo escribir '$path' en el workspace real")
            }
            "edit_file" -> {
                val path = args.string("path") ?: return ToolResult(false, "falta 'path'")
                val diff = args.string("diff") ?: return ToolResult(false, "falta 'diff'")
                val original = WorkspaceManager.readBytes(context, path)?.toString(Charsets.UTF_8)
                    ?: return ToolResult(false, "el archivo '$path' no existe; para crearlo usa write_file")
                val (edited, applied, failed) = applySearchReplace(original, diff)
                if (failed != null) {
                    ToolResult(
                        false,
                        "ningún cambio aplicado: $failed\n" +
                            "El texto SEARCH debe coincidir EXACTAMENTE (incluyendo indentación) con el contenido actual. " +
                            "Lee el archivo de nuevo con read_file y reintenta."
                    )
                } else {
                    val ok = WorkspaceManager.writeBytes(context, path, edited.toByteArray(Charsets.UTF_8))
                    if (ok) ToolResult(true, "editado realmente: $path ($applied bloque(s) SEARCH/REPLACE aplicados, ${edited.toByteArray().size} bytes finales)")
                    else ToolResult(false, "no se pudo escribir el archivo editado '$path'")
                }
            }
            "delete_file" -> {
                val path = args.string("path") ?: return ToolResult(false, "falta 'path'")
                if (WorkspaceManager.delete(context, path)) {
                    ToolResult(true, "eliminado realmente: $path")
                } else {
                    ToolResult(false, "no se pudo eliminar '$path' (¿existe?)")
                }
            }
            "move_file" -> {
                val from = args.string("from") ?: return ToolResult(false, "falta 'from'")
                val to = args.string("to") ?: return ToolResult(false, "falta 'to'")
                if (WorkspaceManager.move(context, from, to)) ToolResult(true, "movido realmente: $from → $to")
                else ToolResult(false, "no se pudo mover '$from' a '$to'")
            }
            "search_files" -> {
                val query = args.string("query") ?: return ToolResult(false, "falta 'query'")
                val results = WorkspaceManager.search(context, query)
                if (results.isEmpty()) ToolResult(true, "sin coincidencias reales para '$query'")
                else ToolResult(true, results.joinToString("\n") { (if (it.isDirectory) "[dir]  " else "[file] ") + it.path })
            }
            "run_process" -> {
                val command = args.string("command") ?: return ToolResult(false, "falta 'command'")
                val timeoutSec = args.int("timeout_sec")?.coerceIn(5, 1200) ?: 120
                runInLinux(projectPath, command, timeoutSec * 1000L)
            }
            "install_dependency" -> {
                val command = args.string("command") ?: return ToolResult(false, "falta 'command'")
                val allowed = command.trimStart().startsWith("apt-get ") ||
                    command.trimStart().startsWith("apt ") ||
                    command.trimStart().startsWith("pip") ||
                    command.trimStart().startsWith("./gradlew") ||
                    command.trimStart().startsWith("wget ") ||
                    command.trimStart().startsWith("unzip ") ||
                    command.trimStart().startsWith("update-alternatives ")
                if (!allowed) {
                    return ToolResult(false, "install_dependency solo acepta apt/apt-get/pip/gradlew/wget/unzip. Para otra cosa usa run_process.")
                }
                runInLinux(projectPath, command, 600_000L)
            }
            "build_project" -> {
                val project = projectPath ?: return ToolResult(false, "no hay proyecto activo")
                val system = WorkspaceManager.detectBuildSystem(context, project)
                    ?: return ToolResult(false, "no se detecta un sistema de build real en '$project' (sin settings.gradle, Makefile, requirements.txt o package.json)")
                val task = args.string("task")
                val commands = buildCommands(system, task)
                executeBuildSequence(project, system, commands)
            }
            "run_tests" -> {
                val project = projectPath ?: return ToolResult(false, "no hay proyecto activo")
                val system = WorkspaceManager.detectBuildSystem(context, project)
                    ?: return ToolResult(false, "sin sistema de build detectado en '$project'")
                val extra = args.string("args").orEmpty()
                val commands = when (system) {
                    "android-gradle", "gradle" -> listOf("./gradlew test $extra --console=plain".trim())
                    "python" -> listOf("python3 -m pytest $extra -q".trim())
                    "make" -> listOf("make test $extra".trim())
                    else -> return ToolResult(false, "sin comando de pruebas conocido para '$system'")
                }
                executeBuildSequence(project, "test:$system", commands)
            }
            "create_task" -> {
                val title = args.string("title") ?: return ToolResult(false, "falta 'title'")
                val project = projectPath ?: return ToolResult(false, "no hay proyecto activo")
                val id = "task_${System.currentTimeMillis()}"
                val projectId = projectNameToId(project)
                taskDao.insertAll(
                    listOf(
                        com.example.data.TaskEntity(
                            id = id,
                            projectId = projectId,
                            title = title,
                            status = ModelTaskStatus.PENDIENTE,
                            assignedAgent = args.string("assigned_agent") ?: agentName
                        )
                    )
                )
                ToolResult(true, "tarea creada en el proyecto real: id=$id «$title»")
            }
            "complete_task" -> {
                val taskId = args.string("task_id") ?: return ToolResult(false, "falta 'task_id'")
                val tasks = taskDao.getAll()
                val target = tasks.firstOrNull { it.id == taskId }
                    ?: return ToolResult(false, "no existe la tarea '$taskId'. Tareas reales: ${tasks.joinToString { "${it.id}=${it.title}" }.ifBlank { "ninguna" }}")
                taskDao.insertAll(
                    listOf(target.copy(status = ModelTaskStatus.COMPLETADA))
                )
                ToolResult(true, "tarea completada: «${target.title}»")
            }
            "report_issue" -> {
                val project = projectPath ?: return ToolResult(false, "no hay proyecto activo")
                val problem = args.string("problem") ?: return ToolResult(false, "falta 'problem'")
                val expected = args.string("expected") ?: ""
                val actual = args.string("actual") ?: ""
                val evidence = args.string("evidence") ?: ""
                val now = System.currentTimeMillis()
                val id = "issue_${now}"
                issueDao.upsert(
                    IssueEntity(
                        id = id,
                        projectId = projectNameToId(project),
                        severity = parseSeverity(args.string("severity")),
                        category = args.string("category") ?: "general",
                        requirement = args.string("requirement") ?: "",
                        problem = problem,
                        expected = expected,
                        actual = actual,
                        evidence = evidence,
                        file = args.string("file") ?: "",
                        line = args.int("line") ?: 0,
                        reproduction = args.string("reproduction") ?: "",
                        status = if (evidence.isBlank()) IssueStatus.UNVERIFIED else IssueStatus.VERIFIED,
                        createdAtMillis = now,
                        updatedAtMillis = now
                    )
                )
                ToolResult(
                    true,
                    "issue registrado (${id}): ${args.string("severity") ?: "sin severidad"} — $problem" +
                        if (evidence.isBlank()) " [sin evidencia: queda UNVERIFIED]" else " [con evidencia]"
                )
            }
            else -> ToolResult(false, "herramienta no implementada: ${spec.name}")
        }
    }

    private suspend fun runInLinux(projectPath: String?, command: String, timeoutMs: Long): ToolResult {
        val since = System.currentTimeMillis()
        val pushed = projectPath?.let { WorkspaceBridge.push(context, it) } ?: 0
        val effective = if (projectPath != null) {
            "cd '${WorkspaceBridge.guestPathFor(projectPath)}' 2>/dev/null || cd ~; $command"
        } else command
        val result = ExecutionEngine.run(context, effective, cwd = "~", timeoutMs = timeoutMs)
        val imported = projectPath?.let { WorkspaceBridge.pull(context, it, since) } ?: emptyList()
        val detail = buildString {
            append("Comando real ejecutado en el entorno Linux:\n$ ")
            append(command)
            append("\n\n")
            if (pushed > 0) append("(sincronizados $pushed archivos del proyecto al entorno antes de ejecutar)\n\n")
            append(result.tail(150))
            if (imported.isNotEmpty()) {
                append("\n\nArtefactos importados al workspace del usuario: ${imported.take(20).joinToString(", ")}" +
                    if (imported.size > 20) " (+${imported.size - 20} más)" else "")
            }
        }
        return ToolResult(result.success, detail, dataJson = """{"exitCode":${result.exitCode}}""")
    }

    private suspend fun executeBuildSequence(
        projectPath: String,
        system: String,
        commands: List<String>
    ): ToolResult {
        val recordId = "build_${System.currentTimeMillis()}"
        val startedAt = System.currentTimeMillis()
        buildDao.upsert(
            BuildRecordEntity(
                id = recordId,
                projectId = projectNameToId(projectPath),
                command = commands.joinToString(" && "),
                state = "BUILD_RUNNING",
                exitCode = null,
                artifactPath = null,
                logTail = "",
                startedAtMillis = startedAt,
                finishedAtMillis = null
            )
        )
        val since = System.currentTimeMillis()
        WorkspaceBridge.push(context, projectPath)
        var lastResult: ExecutionEngine.ExecResult? = null
        for (command in commands) {
            lastResult = ExecutionEngine.run(context, command, timeoutMs = 1_200_000L)
            if (!lastResult.success) break
        }
        val imported = WorkspaceBridge.pull(context, projectPath, since)
        val result = lastResult
        val state = when {
            result == null -> "BUILD_FAILED"
            result.success -> "BUILD_SUCCEEDED"
            else -> "BUILD_FAILED"
        }
        val artifact = imported.firstOrNull {
            it.endsWith(".apk") || it.endsWith(".jar") || it.endsWith(".zip") || it.endsWith(".bin")
        }
        buildDao.upsert(
            BuildRecordEntity(
                id = recordId,
                projectId = projectNameToId(projectPath),
                command = commands.joinToString(" && "),
                state = state,
                exitCode = result?.exitCode,
                artifactPath = artifact?.let { "$projectPath/$it" },
                logTail = result?.tail(60) ?: "",
                startedAtMillis = startedAt,
                finishedAtMillis = System.currentTimeMillis()
            )
        )
        val detail = buildString {
            append("BUILD_RUNNING → $state (sistema: $system)\n\n")
            append(result?.tail(150) ?: "sin salida")
            artifact?.let {
                append("\n\nArtefacto real generado: $projectPath/$it (importado al workspace del usuario)")
            }
        }
        return ToolResult(result?.success == true, detail)
    }

    private fun buildCommands(system: String, task: String?): List<String> = when (system) {
        "android-gradle", "gradle" -> {
            val gradleTask = task ?: "assembleDebug"
            listOf(
                "if ! command -v javac >/dev/null 2>&1; then apt-get update -qq && DEBIAN_FRONTEND=noninteractive apt-get install -y -qq openjdk-17-jdk-headless; fi",
                ANDROID_SDK_BOOTSTRAP,
                "if [ -f ./gradlew ]; then ./gradlew $gradleTask --no-daemon --console=plain; else gradle $gradleTask --console=plain; fi"
            )
        }
        "python" -> listOf(
            "if [ -f requirements.txt ]; then pip3 install -r requirements.txt; fi",
            "python3 -m compileall -q . && echo 'compilación python OK'"
        )
        "make" -> listOf("make ${task ?: ""}".trim())
        "node" -> listOf("if [ -f package.json ]; then (npm run build 2>/dev/null || npm install); fi")
        else -> listOf("echo 'sistema de build no soportado: $system'")
    }

    private fun approvalTitle(tool: String) = when (tool) {
        "delete_file" -> "Eliminar archivos reales"
        "install_dependency" -> "Instalar dependencia en el entorno Linux"
        "build_project" -> "Compilar el proyecto en el entorno Linux"
        "run_tests" -> "Ejecutar pruebas en el entorno Linux"
        "run_process" -> "Ejecutar comando en el entorno Linux"
        "move_file" -> "Mover o renombrar archivos reales"
        else -> "Ejecutar $tool"
    }

    private fun affectedOf(call: ToolCall): List<String> = try {
        val args = JsonArgs(call.argumentsJson)
        listOfNotNull(args.string("path") ?: args.string("from") ?: args.string("command"))
    } catch (_: Throwable) {
        emptyList()
    }

    private fun parseSeverity(raw: String?): IssueSeverity = try {
        raw?.let { IssueSeverity.valueOf(it.uppercase()) } ?: IssueSeverity.MAJOR
    } catch (_: IllegalArgumentException) {
        IssueSeverity.MAJOR
    }

    /** Convierte la ruta del proyecto en el id usado en Room ("projects/demo" → "demo"). */
    private fun projectNameToId(projectPath: String): String = projectPath.trim('/').substringAfterLast('/')

    /**
     * Preparación real del SDK de Android dentro del entorno Linux, adaptada de
     * las recetas probadas de builds on-device (Termux/PRoot). Descarga las
     * commandline-tools oficiales, acepta licencias e instala platform +
     * build-tools; es idempotente y solo actúa si falta ANDROID_HOME.
     */
    private val ANDROID_SDK_BOOTSTRAP =
        "if [ -z \"\$ANDROID_HOME\" ] && [ ! -d /opt/android-sdk ]; then " +
            "mkdir -p /opt/android-sdk/cmdline-tools && " +
            "wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/clt.zip && " +
            "unzip -q /tmp/clt.zip -d /tmp/clt && mv /tmp/clt/cmdline-tools /opt/android-sdk/cmdline-tools/latest && " +
            "yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk --licenses >/dev/null && " +
            "/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk \"platform-tools\" \"platforms;android-34\" \"build-tools;34.0.0\" >/dev/null; " +
            "fi; export ANDROID_HOME=\${ANDROID_HOME:-/opt/android-sdk}"

    /**
     * Aplica bloques SEARCH/REPLACE exactos (formato Aider) sobre el texto
     * original. Devuelve (texto_editado, bloques_aplicados, fallo). Si un
     * bloque no coincide, no se aplica nada (atomicidad).
     */
    private fun applySearchReplace(
        original: String,
        diff: String
    ): Triple<String, Int, String?> {
        val blocks = diff.split("<<<<<<< SEARCH")
            .drop(1)
            .map { section ->
                val inner = section.substringBefore(">>>>>>> REPLACE")
                val parts = inner.split("=======" )
                if (parts.size < 2) return Triple(original, 0, "bloque SEARCH/REPLACE malformado (falta ======= o >>>>>>> REPLACE)")
                parts[0].trimStart('\n', ' ') to parts.drop(1).joinToString("=").trimStart('\n', ' ')
            }
        if (blocks.isEmpty()) return Triple(original, 0, "no se encontró ningún bloque <<<<<<< SEARCH")

        var text = original
        for ((index, block) in blocks.withIndex()) {
            val (search, replace) = block
            val searchNorm = search.trimEnd('\n')
            val replaceNorm = replace.trimEnd('\n')
            when {
                text.contains(searchNorm) -> text = text.replace(searchNorm, replaceNorm)
                text.contains(search.trim()) -> text = text.replace(search.trim(), replace.trim())
                else -> return Triple(
                    original,
                    0,
                    "el bloque ${index + 1} no coincide con el archivo actual (SEARCH: «${searchNorm.take(80)}»…)"
                )
            }
        }
        return Triple(text, blocks.size, null)
    }
}

/** Lectura tolerante de argumentos JSON de function calling. */
class JsonArgs(raw: String) {
    private val map: Map<String, Any> = try {
        val moshi = com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
        @Suppress("UNCHECKED_CAST")
        moshi.adapter(Map::class.java).fromJson(raw) as? Map<String, Any> ?: emptyMap()
    } catch (_: Throwable) {
        emptyMap()
    }

    fun string(key: String): String? = (map[key] as? String)?.takeIf { it.isNotBlank() }
        ?: (map[key] as? Double)?.toString()

    fun int(key: String): Int? = when (val v = map[key]) {
        is Double -> v.toInt()
        is Long -> v.toInt()
        is String -> v.toIntOrNull()
        else -> null
    }
}
