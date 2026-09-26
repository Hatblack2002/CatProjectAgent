package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIProvider
import com.example.ai.GeminiProvider
import com.example.ai.ProviderRegistry
import com.example.agents.AgentEngine
import com.example.data.CatRepository
import com.example.data.WorkspaceRepository
import com.example.data.formatRelativeActivity
import com.example.data.formatTimeOfDay
import com.example.engine.ExecutionEngine
import com.example.model.ActionApprovalRequest
import com.example.model.AgentStatus
import com.example.model.CatAgent
import com.example.model.CatChatMessage
import com.example.model.CatFileItem
import com.example.model.CatNavTab
import com.example.model.CatProject
import com.example.model.ProjectCategory
import com.example.model.ProjectStatus
import com.example.tools.ToolGateway
import com.example.workspace.WorkspaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

enum class CatScreenRoute {
    SPLASH,
    MAIN_TABS,
    NEW_PROJECT,
    PROJECT_DETAIL,
    AGENT_CHAT,
    SETTINGS
}

data class SetupState(
    val providerConfigured: Boolean = false,
    val providerDetail: String = "",
    val workspaceConfigured: Boolean = false,
    val workspaceLabel: String = "",
    val environmentReady: Boolean = false,
    val environmentDetail: String = "",
    val checking: Boolean = true
)

data class CatUiState(
    val currentRoute: CatScreenRoute = CatScreenRoute.SPLASH,
    val selectedTab: CatNavTab = CatNavTab.INICIO,
    val projects: List<CatProject> = emptyList(),
    val projectFilter: String = "Todos",
    val projectSearchQuery: String = "",
    val selectedProjectId: String? = null,
    val agents: List<CatAgent> = emptyList(),
    val selectedAgentId: String? = DEFAULT_AGENT_ID,
    val files: List<CatFileItem> = emptyList(),
    val fileFilter: String = "Todos",
    val fileSearchQuery: String = "",
    val chatMessages: List<CatChatMessage> = emptyList(),
    val currentChatInput: String = "",
    val isAgentTyping: Boolean = false,
    val isDarkMode: Boolean = true,
    val selectedCategoryTemplate: ProjectCategory = ProjectCategory.TODO,
    val newProjectName: String = "",
    val newProjectDescription: String = "",
    val selectedTemplateName: String = "Proyecto desde cero",
    val showApprovalSheet: Boolean = false,
    val pendingApproval: ActionApprovalRequest? = null,
    val toastMessage: String? = null,
    val setup: SetupState = SetupState(),
    val filePreviewName: String? = null,
    val filePreviewContent: String? = null,
    val cycleRunning: Boolean = false,
    val cycleProgress: String? = null,
    val openIssues: Int = 0
)

class CatProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val repository = CatRepository(appContext)
    private val provider: AIProvider = ProviderRegistry.gemini(appContext)

    private val _uiState = MutableStateFlow(CatUiState())
    val uiState: StateFlow<CatUiState> = _uiState.asStateFlow()

    val bootstrapStatus: StateFlow<com.example.service.LinuxBootstrap.Status>
        get() = com.example.service.LinuxBootstrap.status

    private val pendingCommands = mutableMapOf<String, String>()
    private val approvalMutex = Mutex()

    private val gateway = ToolGateway(appContext, object : ToolGateway.ApprovalGate {
        override suspend fun requestApproval(request: ToolGateway.ApprovalContext): Boolean {
            val deferred = CompletableDeferred<Boolean>()
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        showApprovalSheet = true,
                        pendingApproval = ActionApprovalRequest(
                            id = UUID.randomUUID().toString(),
                            agentName = request.agentName,
                            actionTitle = request.actionTitle,
                            reason = request.reason,
                            affectedFiles = request.affected,
                            actionType = ACTION_TOOL
                        )
                    )
                }
            }
            approvalMutex.withLock {
                activeApproval = deferred
            }
            val approved = deferred.await()
            activeApproval = null
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(showApprovalSheet = false, pendingApproval = null) }
            }
            return approved
        }
    })

    @Volatile
    private var activeApproval: CompletableDeferred<Boolean>? = null

    private val agentListener = object : AgentEngine.Listener {
        override fun onToolExecution(agentName: String, tool: String, summary: String) {
            _uiState.update {
                it.copy(cycleProgress = "$agentName → $tool")
            }
        }
    }

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { WorkspaceRepository.ensureBase(appContext) }
            repository.seedDefaultAgents()
            refreshProjects()
            refreshAgents()
            refreshChat()
            refreshFiles()
            refreshSetup()
        }
        startSplashCountdown()
    }

    private fun refreshSetup() {
        viewModelScope.launch(Dispatchers.IO) {
            val providerOk = provider.isConfigured()
            val providerDetail = if (providerOk) {
                val test = provider.testConnection()
                test.detail
            } else {
                "falta GEMINI_API_KEY en el archivo .env del proyecto (recompila después de definirla)"
            }
            _uiState.update {
                it.copy(
                    setup = it.setup.copy(
                        providerConfigured = providerOk,
                        providerDetail = providerDetail,
                        workspaceConfigured = WorkspaceManager.hasUserWorkspace(appContext),
                        workspaceLabel = WorkspaceManager.workspaceLabel(appContext),
                        environmentReady = com.example.service.PtyBridge.isEngineReady(appContext),
                        environmentDetail = ExecutionEngine.describe(
                            com.example.service.LinuxBootstrap.status.value
                        ),
                        checking = false
                    )
                )
            }
        }
    }

    fun recheckSetup() = refreshSetup()

    fun onWorkspaceTreePicked(uri: Uri) {
        val saved = WorkspaceManager.saveTree(appContext, uri)
        _uiState.update {
            it.copy(
                toastMessage = if (saved) "Workspace real configurado: ${WorkspaceManager.workspaceLabel(appContext)}"
                else "No se pudo conservar el permiso sobre esa carpeta",
                setup = it.setup.copy(
                    workspaceConfigured = WorkspaceManager.hasUserWorkspace(appContext),
                    workspaceLabel = WorkspaceManager.workspaceLabel(appContext)
                )
            )
        }
        refreshFiles()
    }

    fun clearWorkspace() {
        WorkspaceManager.clearTree(appContext)
        _uiState.update {
            it.copy(
                toastMessage = "Workspace del usuario desvinculado; se usa el workspace privado (archivos reales)",
                setup = it.setup.copy(
                    workspaceConfigured = false,
                    workspaceLabel = WorkspaceManager.workspaceLabel(appContext)
                )
            )
        }
        refreshFiles()
    }

    private fun refreshProjects() {
        viewModelScope.launch {
            val projects = repository.loadProjects()
            _uiState.update { it.copy(projects = projects) }
        }
    }

    private fun refreshAgents() {
        viewModelScope.launch {
            val agents = repository.loadAgents()
            _uiState.update { it.copy(agents = agents) }
        }
    }

    private fun refreshChat() {
        viewModelScope.launch {
            val messages = repository.loadChat()
            _uiState.update { it.copy(chatMessages = messages) }
        }
    }

    private fun refreshFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val items = WorkspaceManager.listForUser(appContext)
            _uiState.update { it.copy(files = items) }
        }
    }

    private fun startSplashCountdown() {
        viewModelScope.launch {
            delay(2200)
            _uiState.update { it.copy(currentRoute = CatScreenRoute.MAIN_TABS) }
        }
    }

    fun selectTab(tab: CatNavTab) {
        if (tab == CatNavTab.ARCHIVOS) refreshFiles()
        _uiState.update { it.copy(selectedTab = tab, currentRoute = CatScreenRoute.MAIN_TABS) }
    }

    fun navigateTo(route: CatScreenRoute) {
        _uiState.update { it.copy(currentRoute = route) }
    }

    fun openProjectDetail(projectId: String) {
        _uiState.update { it.copy(selectedProjectId = projectId, currentRoute = CatScreenRoute.PROJECT_DETAIL) }
        viewModelScope.launch {
            val issues = repository.loadIssues(projectId)
            val open = issues.count { it.status != com.example.data.IssueStatus.FIXED && it.status != com.example.data.IssueStatus.REFUTED }
            _uiState.update { it.copy(openIssues = open) }
        }
    }

    fun openAgentChat(agentId: String) {
        _uiState.update { it.copy(selectedAgentId = agentId, currentRoute = CatScreenRoute.AGENT_CHAT) }
    }

    fun setProjectFilter(filter: String) {
        _uiState.update { it.copy(projectFilter = filter) }
    }

    fun setProjectSearchQuery(query: String) {
        _uiState.update { it.copy(projectSearchQuery = query) }
    }

    fun setFileFilter(filter: String) {
        _uiState.update { it.copy(fileFilter = filter) }
    }

    fun setFileSearchQuery(query: String) {
        _uiState.update { it.copy(fileSearchQuery = query) }
    }

    fun onChatInputChange(input: String) {
        _uiState.update { it.copy(currentChatInput = input) }
    }

    private fun activeProjectPath(): String? =
        _uiState.value.selectedProjectId?.let { "projects/$it" }

    fun sendChatMessage(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty()) return
        val agentId = _uiState.value.selectedAgentId ?: DEFAULT_AGENT_ID

        viewModelScope.launch {
            val userMessage = CatChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = "user",
                senderName = "Usuario",
                isUser = true,
                text = prompt,
                timestamp = formatTimeOfDay(System.currentTimeMillis())
            )
            repository.insertChatMessage(userMessage)
            _uiState.update {
                it.copy(chatMessages = it.chatMessages + userMessage, currentChatInput = "", isAgentTyping = true)
            }

            val agent = repository.getAgent(agentId) ?: repository.findAgentByName("Arquitecto")
            if (agent == null) {
                _uiState.update {
                    it.copy(
                        isAgentTyping = false,
                        toastMessage = "No hay agentes disponibles; reinicia la app para regenerar el equipo."
                    )
                }
                return@launch
            }
            val agentName = agent.name
            repository.setAgentStatus(agent.id, AgentStatus.PENSANDO)
            updateAgentStatus(agent.id, AgentStatus.PENSANDO)

            val history = _uiState.value.chatMessages
                .dropLast(1)
                .filter { it.text.isNotBlank() }
                .takeLast(12)
                .map { it.isUser to it.text }

            if (!provider.isConfigured()) {
                postAgentMessage(
                    agent,
                    agentName,
                    "No hay proveedor de IA configurado todavía. Define GEMINI_API_KEY en el archivo .env del proyecto " +
                        "(Secrets Gradle Plugin) y recompila; entonces podré razonar y usar herramientas reales."
                )
                restoreAgent(agent)
                _uiState.update { it.copy(isAgentTyping = false) }
                return@launch
            }

            val engine = AgentEngine(provider, gateway, agentListener)
            val turn = engine.converse(agent, activeProjectPath(), prompt, history)

            val agentMessage = CatChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = agent?.id ?: "agent",
                senderName = agentName,
                isUser = false,
                text = turn.text,
                timestamp = formatTimeOfDay(System.currentTimeMillis())
            )
            repository.insertChatMessage(agentMessage)
            restoreAgent(agent)
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + agentMessage,
                    isAgentTyping = false,
                    cycleProgress = null
                )
            }
            agent?.let { repository.incrementAgentTasks(it.id) }
            refreshProjects()
            refreshFiles()
        }
    }

    /** Ciclo real multiagente: Arquitecto → Programador → Analista. */
    fun runAgentCycle(goal: String) {
        val projectPath = activeProjectPath() ?: run {
            _uiState.update { it.copy(toastMessage = "Abre un proyecto antes de ejecutar el ciclo.") }
            return
        }
        if (!provider.isConfigured()) {
            _uiState.update { it.copy(toastMessage = "Configura el proveedor de IA (GEMINI_API_KEY) antes de ejecutar el ciclo.") }
            return
        }
        if (_uiState.value.cycleRunning) return

        viewModelScope.launch {
            _uiState.update { it.copy(cycleRunning = true, cycleProgress = "Ciclo iniciado") }
            val engine = AgentEngine(provider, gateway, agentListener)
            val architect = repository.findAgentByName("Arquitecto")
            val programmer = repository.findAgentByName("Programador")
            val analyst = repository.findAgentByName("Analista")

            suspend fun runStep(agent: CatAgent?, phase: String, message: String): AgentEngine.AgentTurn? {
                agent ?: return null
                repository.setAgentStatus(agent.id, AgentStatus.TRABAJANDO)
                updateAgentStatus(agent.id, AgentStatus.TRABAJANDO)
                _uiState.update { it.copy(cycleProgress = "$phase: ${agent.name} trabajando") }
                val turn = engine.converse(agent, projectPath, message, emptyList())
                postAgentMessage(agent, agent.name, "[$phase]\n\n${turn.text}")
                repository.setAgentStatus(agent.id, AgentStatus.ACTIVO)
                updateAgentStatus(agent.id, AgentStatus.ACTIVO)
                return turn
            }

            try {
                runStep(
                    architect,
                    "FASE 1/3 · Arquitectura",
                    "Objetivo del ciclo: $goal\n\n" +
                        "Analiza el proyecto real (inspect_project), define la especificación y el plan de tareas " +
                        "en archivos reales dentro del proyecto (por ejemplo ESPECIFICACION.md y PLAN.md con write_file) " +
                        "y crea las tareas reales con create_task."
                )
                runStep(
                    programmer,
                    "FASE 2/3 · Implementación",
                    "Ejecuta el plan real: lee la especificación y las tareas, escribe el código real con write_file, " +
                        "instala dependencias si hace falta (install_dependency) y compila con build_project hasta que el " +
                        "build sea real y exitoso."
                )
                runStep(
                    analyst,
                    "FASE 3/3 · Verificación",
                    "Verifica la implementación real: inspecciona archivos, ejecuta run_tests/build_project si corresponde " +
                        "y registra cada problema con report_issue (con evidencia real). Declara el veredicto final del ciclo."
                )
            } finally {
                architect?.let { repository.incrementAgentTasks(it.id) }
                _uiState.update {
                    it.copy(
                        cycleRunning = false,
                        cycleProgress = null,
                        toastMessage = "Ciclo multiagente terminado: revisa el chat y los archivos reales"
                    )
                }
                refreshProjects()
                refreshFiles()
                _uiState.value.selectedProjectId?.let { pid ->
                    val issues = repository.loadIssues(pid)
                    _uiState.update {
                        it.copy(openIssues = issues.count { i ->
                            i.status != com.example.data.IssueStatus.FIXED &&
                                i.status != com.example.data.IssueStatus.REFUTED
                        })
                    }
                }
            }
        }
    }

    fun approvePendingAction() {
        val request = _uiState.value.pendingApproval
        _uiState.update { it.copy(showApprovalSheet = false, pendingApproval = null) }
        request ?: return
        when (request.actionType) {
            ACTION_BOOTSTRAP -> startBootstrap()
            ACTION_RUN_COMMAND -> {
                val command = pendingCommands.remove(request.id)
                if (command.isNullOrBlank()) {
                    _uiState.update { it.copy(toastMessage = "El comando solicitado ya no está disponible.") }
                } else {
                    executeAgentCommand(request.agentName, command)
                }
            }
            ACTION_TOOL -> {
                activeApproval?.complete(true)
            }
        }
    }

    fun denyPendingAction() {
        _uiState.value.pendingApproval?.id?.let { pendingCommands.remove(it) }
        val wasTool = _uiState.value.pendingApproval?.actionType == ACTION_TOOL
        _uiState.update {
            it.copy(
                showApprovalSheet = false,
                pendingApproval = null,
                toastMessage = if (wasTool) null else "Acción rechazada por el usuario."
            )
        }
        if (wasTool) activeApproval?.complete(false)
    }

    fun dismissApprovalSheet() {
        val wasTool = _uiState.value.pendingApproval?.actionType == ACTION_TOOL
        _uiState.value.pendingApproval?.id?.let { pendingCommands.remove(it) }
        _uiState.update { it.copy(showApprovalSheet = false) }
        if (wasTool) activeApproval?.complete(false)
    }

    /** Solicitud real de bootstrap del entorno (instalación de Ubuntu si falta). */
    fun requestEnvironmentSetup() {
        if (com.example.service.PtyBridge.isEngineReady(appContext)) {
            _uiState.update {
                it.copy(toastMessage = "El entorno Linux ya está listo. Solicita acciones al agente desde el chat.")
            }
            return
        }
        _uiState.update {
            it.copy(
                showApprovalSheet = true,
                pendingApproval = ActionApprovalRequest(
                    id = UUID.randomUUID().toString(),
                    agentName = "Programador",
                    actionTitle = "Instalar el entorno Linux (Ubuntu 24.04)",
                    reason = "El entorno real todavía no está instalado. Se descargará la imagen oficial de Ubuntu (~200 MB), " +
                        "se verificará su hash SHA256 y se extraerá en el almacenamiento privado de la app.",
                    affectedFiles = listOf(com.example.service.LinuxBootstrap.rootfsDir(appContext).absolutePath),
                    actionType = ACTION_BOOTSTRAP
                )
            )
        }
    }

    private fun startBootstrap() {
        _uiState.update {
            it.copy(toastMessage = "Instalando el entorno Linux. La descarga continúa en segundo plano.")
        }
        viewModelScope.launch {
            when (val status = com.example.service.LinuxBootstrap.ensure(appContext)) {
                is com.example.service.LinuxBootstrap.Status.Ready ->
                    _uiState.update { it.copy(toastMessage = "Entorno Linux listo: ${status.info.osPrettyName}.") }
                is com.example.service.LinuxBootstrap.Status.Failed ->
                    _uiState.update { it.copy(toastMessage = "La instalación del entorno falló: ${status.reason}") }
                else ->
                    _uiState.update {
                        it.copy(toastMessage = "Estado del entorno: ${ExecutionEngine.describe(status)}")
                    }
            }
            refreshSetup()
        }
    }

    private fun executeAgentCommand(agentName: String, command: String) {
        viewModelScope.launch {
            val agent = repository.findAgentByName(agentName)
            val result = ExecutionEngine.run(appContext, command)
            val text = buildString {
                append("Comando ejecutado en el entorno real:\n\n$command\n\n")
                append(result.tail())
                result.exitCode?.let { append("\n\ncódigo de salida: $it") }
            }
            postAgentMessage(agent, agentName, text)
            refreshProjects()
            refreshFiles()
            _uiState.update { it.copy(toastMessage = "Comando ejecutado.") }
        }
    }

    fun openFilePreview(item: CatFileItem) {
        if (item.isDirectory) {
            _uiState.update { it.copy(toastMessage = "${item.name}: directorio (${item.itemCount ?: 0} elementos)") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = WorkspaceManager.readBytes(appContext, item.path)
            val content = bytes?.toString(Charsets.UTF_8)?.takeIf { isProbablyText(it) }
            _uiState.update {
                it.copy(
                    filePreviewName = item.name,
                    filePreviewContent = content ?: "(contenido binario o ilegible como texto; tamaño real: ${item.size ?: "?"})"
                )
            }
        }
    }

    fun closeFilePreview() {
        _uiState.update { it.copy(filePreviewName = null, filePreviewContent = null) }
    }

    private fun isProbablyText(sample: String): Boolean {
        if (sample.isEmpty()) return true
        val controlChars = sample.take(2000).count { it.code < 9 || (it.code in 14..31) }
        return controlChars <= 2
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
        appContext.getSharedPreferences("cat_settings", android.content.Context.MODE_PRIVATE)
            .edit().putBoolean("dark_mode", _uiState.value.isDarkMode).apply()
    }

    private fun updateAgentStatus(agentId: String, status: AgentStatus) {
        _uiState.update { state ->
            state.copy(agents = state.agents.map {
                if (it.id == agentId) it.copy(status = status) else it
            })
        }
    }

    private suspend fun restoreAgent(agent: CatAgent?) {
        agent ?: return
        repository.setAgentStatus(agent.id, AgentStatus.ACTIVO)
        updateAgentStatus(agent.id, AgentStatus.ACTIVO)
    }

    private suspend fun postAgentMessage(agent: CatAgent?, agentName: String, text: String) {
        val message = CatChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = agent?.id ?: "engine",
            senderName = agentName,
            isUser = false,
            text = text,
            timestamp = formatTimeOfDay(System.currentTimeMillis())
        )
        repository.insertChatMessage(message)
        _uiState.update { it.copy(chatMessages = it.chatMessages + message) }
    }

    fun onNewProjectNameChange(name: String) {
        _uiState.update { it.copy(newProjectName = name) }
    }

    fun onNewProjectDescriptionChange(description: String) {
        _uiState.update { it.copy(newProjectDescription = description) }
    }

    fun onCategoryTemplateSelect(category: ProjectCategory) {
        _uiState.update { it.copy(selectedCategoryTemplate = category) }
    }

    fun onTemplateSelect(template: String) {
        _uiState.update { it.copy(selectedTemplateName = template) }
    }

    fun createProject() {
        val state = _uiState.value
        val name = state.newProjectName.trim().ifBlank { "Nuevo Proyecto" }
        val description = state.newProjectDescription.trim()
            .ifBlank { "Proyecto creado con la plantilla ${state.selectedTemplateName}" }
        val projectId = "proj_${System.currentTimeMillis()}"

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                WorkspaceManager.writeBytes(
                    appContext,
                    "projects/$projectId/README.md",
                    "# $name\n\n$description\n\nCreado el ${formatRelativeActivity(System.currentTimeMillis())} por CatProjectAgent.\n"
                        .toByteArray()
                )
            }
            repository.insertProject(
                CatProject(
                    id = projectId,
                    name = name,
                    description = description,
                    category = state.selectedCategoryTemplate,
                    status = ProjectStatus.EN_PROGRESO,
                    lastActivity = formatRelativeActivity(System.currentTimeMillis()),
                    progressPercent = 0,
                    tasks = emptyList(),
                    filesCount = 1,
                    accentColorHex = BRAND_AMBER
                )
            )
            refreshProjects()
            _uiState.update {
                it.copy(
                    selectedProjectId = projectId,
                    currentRoute = CatScreenRoute.PROJECT_DETAIL,
                    newProjectName = "",
                    newProjectDescription = "",
                    toastMessage = "Proyecto '$name' creado con archivo real en el workspace"
                )
            }
        }
    }
}

private const val DEFAULT_AGENT_ID = "agent_architect"
private const val ACTION_RUN_COMMAND = "RUN_COMMAND"
private const val ACTION_BOOTSTRAP = "BOOTSTRAP"
private const val ACTION_TOOL = "TOOL"
private const val BRAND_AMBER = 0xFFF5A623
