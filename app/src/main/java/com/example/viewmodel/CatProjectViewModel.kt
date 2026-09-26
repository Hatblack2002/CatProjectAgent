package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiClient
import com.example.data.CatRepository
import com.example.data.WorkspaceRepository
import com.example.data.formatRelativeActivity
import com.example.data.formatTimeOfDay
import com.example.model.ActionApprovalRequest
import com.example.model.AgentStatus
import com.example.model.CatAgent
import com.example.model.CatChatMessage
import com.example.model.CatFileItem
import com.example.model.CatNavTab
import com.example.model.CatProject
import com.example.model.ProjectCategory
import com.example.model.ProjectStatus
import com.example.service.LinuxBootstrap
import com.example.service.TerminalEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class CatScreenRoute {
    SPLASH,
    MAIN_TABS,
    NEW_PROJECT,
    PROJECT_DETAIL,
    AGENT_CHAT,
    SETTINGS
}

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
    val toastMessage: String? = null
)

class CatProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val repository = CatRepository(appContext)

    private val _uiState = MutableStateFlow(CatUiState())
    val uiState: StateFlow<CatUiState> = _uiState.asStateFlow()

    val bootstrapStatus: StateFlow<LinuxBootstrap.Status> get() = LinuxBootstrap.status

    private val pendingCommands = mutableMapOf<String, String>()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { WorkspaceRepository.ensureBase(appContext) }
            repository.seedDefaultAgents()
            refreshProjects()
            refreshAgents()
            refreshChat()
            refreshFiles()
        }
        startSplashCountdown()
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
            val items = WorkspaceRepository.list(appContext)
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
            val agentName = agent?.name ?: "Arquitecto"
            agent?.let {
                repository.setAgentStatus(it.id, AgentStatus.PENSANDO)
                updateAgentStatus(it.id, AgentStatus.PENSANDO)
            }

            val history = _uiState.value.chatMessages
                .dropLast(1)
                .filter { it.text.isNotBlank() }
                .takeLast(12)
                .map { GeminiClient.ChatTurn(it.isUser, it.text) }
            val project = _uiState.value.projects.firstOrNull { it.id == _uiState.value.selectedProjectId }
            val projectContext = project?.let {
                "${it.name} (${it.status.label}, progreso ${it.progressPercent}%): ${it.description}"
            }

            when (val result = GeminiClient.generate(
                agentName = agentName,
                agentRole = agent?.role ?: "asistente",
                agentDescription = agent?.description ?: "",
                history = history,
                userMessage = prompt,
                projectContext = projectContext
            )) {
                is GeminiClient.Result.Success -> {
                    val reply = result.reply
                    val agentMessage = CatChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = agent?.id ?: "agent",
                        senderName = agentName,
                        isUser = false,
                        text = reply.text,
                        timestamp = formatTimeOfDay(System.currentTimeMillis()),
                        quickReplies = reply.quickReplies
                    )
                    repository.insertChatMessage(agentMessage)
                    restoreAgent(agent)
                    _uiState.update { state ->
                        state.copy(chatMessages = state.chatMessages + agentMessage, isAgentTyping = false)
                    }
                    reply.proposedCommand?.let { command -> requestCommandApproval(agentName, command) }
                }
                is GeminiClient.Result.Failure -> {
                    postAgentMessage(agent, agentName, "No pude completar la respuesta: ${result.reason}")
                    restoreAgent(agent)
                    _uiState.update { it.copy(isAgentTyping = false) }
                }
            }
        }
    }

    private fun requestCommandApproval(agentName: String, command: String) {
        val request = ActionApprovalRequest(
            id = UUID.randomUUID().toString(),
            agentName = agentName,
            actionTitle = "Ejecutar comando en el entorno Linux",
            reason = "$agentName solicita ejecutar este comando en el entorno real de Ubuntu. La salida se añadirá a la conversación.",
            affectedFiles = listOf(command),
            actionType = ACTION_RUN_COMMAND
        )
        pendingCommands[request.id] = command
        _uiState.update { it.copy(showApprovalSheet = true, pendingApproval = request) }
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
            withContext(Dispatchers.IO) { WorkspaceRepository.createProjectDir(appContext, projectId) }
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
                    filesCount = 0,
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
                    toastMessage = "Proyecto '$name' creado"
                )
            }
        }
    }


    fun triggerSampleApprovalRequest() {
        if (!LinuxBootstrap.isReady(appContext)) {
            _uiState.update {
                it.copy(
                    showApprovalSheet = true,
                    pendingApproval = ActionApprovalRequest(
                        id = UUID.randomUUID().toString(),
                        agentName = "Programador",
                        actionTitle = "Instalar el entorno Linux (Ubuntu 24.04)",
                        reason = "El entorno real todavía no está instalado. Se descargará la imagen oficial de Ubuntu (~200 MB), " +
                            "se verificará su hash SHA256 y se extraerá en el almacenamiento privado de la app.",
                        affectedFiles = listOf(LinuxBootstrap.rootfsDir(appContext).absolutePath),
                        actionType = ACTION_BOOTSTRAP
                    )
                )
            }
            return
        }
        _uiState.update {
            it.copy(toastMessage = "El entorno Linux ya está listo. Solicita acciones al agente desde el chat.")
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
            else -> _uiState.update { it.copy(toastMessage = "Acción aprobada.") }
        }
    }

    fun denyPendingAction() {
        _uiState.value.pendingApproval?.id?.let { pendingCommands.remove(it) }
        _uiState.update {
            it.copy(showApprovalSheet = false, pendingApproval = null, toastMessage = "Acción rechazada por el usuario.")
        }
    }

    fun dismissApprovalSheet() {
        _uiState.value.pendingApproval?.id?.let { pendingCommands.remove(it) }
        _uiState.update { it.copy(showApprovalSheet = false) }
    }

    private fun startBootstrap() {
        _uiState.update {
            it.copy(toastMessage = "Instalando el entorno Linux. La descarga continúa en segundo plano.")
        }
        viewModelScope.launch {
            when (val status = LinuxBootstrap.ensure(appContext)) {
                is LinuxBootstrap.Status.Ready ->
                    _uiState.update { it.copy(toastMessage = "Entorno Linux listo: ${status.info.osPrettyName}.") }
                is LinuxBootstrap.Status.Failed ->
                    _uiState.update { it.copy(toastMessage = "La instalación del entorno falló: ${status.reason}") }
                else ->
                    _uiState.update { it.copy(toastMessage = "Estado del entorno: ${TerminalEngine.describe(status)}") }
            }
        }
    }

    private fun executeAgentCommand(agentName: String, command: String) {
        viewModelScope.launch {
            val agent = repository.findAgentByName(agentName)

            if (!LinuxBootstrap.isReady(appContext)) {
                postAgentMessage(
                    agent,
                    agentName,
                    "El comando requiere el entorno Linux, que no está instalado todavía. " +
                        "Instalando el entorno (descarga oficial de Ubuntu, ~200 MB); el comando se ejecutará al terminar."
                )
                _uiState.update {
                    it.copy(toastMessage = "Instalando el entorno Linux. La descarga continúa en segundo plano.")
                }
                when (val status = LinuxBootstrap.ensure(appContext)) {
                    is LinuxBootstrap.Status.Ready -> Unit
                    is LinuxBootstrap.Status.Failed -> {
                        postAgentMessage(agent, agentName, "La instalación del entorno falló: ${status.reason}. El comando no se ejecutó.")
                        _uiState.update { it.copy(toastMessage = "La instalación del entorno falló.") }
                        return@launch
                    }
                    else -> return@launch
                }
            }

            _uiState.update { it.copy(toastMessage = "Ejecutando en el entorno real...") }
            val result = TerminalEngine.executeCommand(command, "~", appContext, sessionId = ENGINE_SESSION_ID)

            val lines = result.lines.map { it.text }
            val truncated = lines.size > MAX_OUTPUT_LINES
            val visible = if (truncated) lines.takeLast(MAX_OUTPUT_LINES) else lines
            val output = visible.joinToString("\n").trim()
            val text = buildString {
                append("Comando ejecutado en el entorno real:\n\n$command\n\n")
                append(output.ifBlank { "(sin salida)" })
                if (truncated) append("\n\n(salida recortada: se muestran las últimas $MAX_OUTPUT_LINES líneas)")
            }
            postAgentMessage(agent, agentName, text)

            agent?.let {
                repository.incrementAgentTasks(it.id)
                repository.setAgentStatus(it.id, AgentStatus.ACTIVO)
                updateAgentStatus(it.id, AgentStatus.ACTIVO)
            }
            refreshProjects()
            refreshFiles()
            _uiState.update { it.copy(toastMessage = "Comando ejecutado.") }
        }
    }


    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }
}

private const val DEFAULT_AGENT_ID = "agent_architect"
private const val ENGINE_SESSION_ID = "agent-shell"
private const val ACTION_RUN_COMMAND = "RUN_COMMAND"
private const val ACTION_BOOTSTRAP = "BOOTSTRAP"
private const val MAX_OUTPUT_LINES = 150
private const val BRAND_AMBER = 0xFFF5A623
