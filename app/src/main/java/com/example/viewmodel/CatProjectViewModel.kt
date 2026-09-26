package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ActionApprovalRequest
import com.example.model.AgentStatus
import com.example.model.CatAgent
import com.example.model.CatChatMessage
import com.example.model.CatFileItem
import com.example.model.CatNavTab
import com.example.model.CatProject
import com.example.model.ProjectCategory
import com.example.model.ProjectStatus
import com.example.model.ProjectTask
import com.example.model.TaskStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val selectedProjectId: String? = "proj_inventory",
    val agents: List<CatAgent> = emptyList(),
    val selectedAgentId: String? = "agent_architect",
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

    private val _uiState = MutableStateFlow(CatUiState())
    val uiState: StateFlow<CatUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        startSplashCountdown()
    }

    private fun startSplashCountdown() {
        viewModelScope.launch {
            delay(2200)
            _uiState.update { it.copy(currentRoute = CatScreenRoute.MAIN_TABS) }
        }
    }

    private fun loadInitialData() {
        val initialProjects = listOf(
            CatProject(
                id = "proj_inventory",
                name = "App de Inventario",
                description = "Aplicación móvil para gestionar inventario de productos con sincronización en la nube.",
                category = ProjectCategory.APPS,
                status = ProjectStatus.EN_PROGRESO,
                lastActivity = "Hoy, 14:32",
                progressPercent = 65,
                accentColorHex = 0xFF0EA5E9,
                filesCount = 38,
                tasks = listOf(
                    ProjectTask("t1", "Diseño de interfaz", TaskStatus.COMPLETADA, "Diseñador"),
                    ProjectTask("t2", "Base de datos", TaskStatus.EN_PROGRESO, "Programador"),
                    ProjectTask("t3", "Integración de API", TaskStatus.PENDIENTE, "Programador"),
                    ProjectTask("t4", "Pruebas", TaskStatus.PENDIENTE, "Analista")
                )
            ),
            CatProject(
                id = "proj_pixel_game",
                name = "Juego 2D Pixel",
                description = "Plataformas retro con personajes en pixel art y físicas fluidas.",
                category = ProjectCategory.JUEGOS,
                status = ProjectStatus.EN_REVISION,
                lastActivity = "Ayer, 18:20",
                progressPercent = 80,
                accentColorHex = 0xFFA855F7,
                filesCount = 52
            ),
            CatProject(
                id = "proj_blog",
                name = "Blog Personal",
                description = "Portal estático rápido con Markdown y optimización SEO.",
                category = ProjectCategory.WEB,
                status = ProjectStatus.COMPLETADO,
                lastActivity = "Hace 1 día",
                progressPercent = 100,
                accentColorHex = 0xFF10B981,
                filesCount = 19
            ),
            CatProject(
                id = "proj_tasks",
                name = "App de Tareas",
                description = "Gestión de tareas colaborativa con notificaciones y recordatorios.",
                category = ProjectCategory.APPS,
                status = ProjectStatus.EN_PAUSA,
                lastActivity = "Hace 2 días",
                progressPercent = 40,
                accentColorHex = 0xFFF59E0B,
                filesCount = 27
            ),
            CatProject(
                id = "proj_landing",
                name = "Landing Page",
                description = "Página de aterrizaje responsiva con captación de leads.",
                category = ProjectCategory.WEB,
                status = ProjectStatus.EN_PROGRESO,
                lastActivity = "Hace 3 días",
                progressPercent = 50,
                accentColorHex = 0xFF0EA5E9,
                filesCount = 14
            ),
            CatProject(
                id = "proj_management",
                name = "Sistema de Gestión",
                description = "Panel administrativo con métricas, usuarios y reportes en PDF.",
                category = ProjectCategory.OTROS,
                status = ProjectStatus.COMPLETADO,
                lastActivity = "Hace 4 días",
                progressPercent = 100,
                accentColorHex = 0xFF10B981,
                filesCount = 64
            )
        )

        val initialAgents = listOf(
            CatAgent(
                id = "agent_architect",
                name = "Arquitecto",
                role = "Define la estructura y plan del proyecto.",
                description = "Especialista en patrones de diseño, escalabilidad y diagramas de arquitectura.",
                model = "Claude 3.5 Sonnet",
                provider = "Anthropic",
                status = AgentStatus.ACTIVO,
                colorHex = 0xFF0EA5E9,
                tasksCompleted = 42
            ),
            CatAgent(
                id = "agent_designer",
                name = "Diseñador",
                role = "Crea la interfaz y experiencia visual.",
                description = "Diseño de sistemas de color, componentes Compose, tokens de espaciado y animaciones.",
                model = "Gemini 1.5 Pro",
                provider = "Google",
                status = AgentStatus.ACTIVO,
                colorHex = 0xFFA855F7,
                tasksCompleted = 36
            ),
            CatAgent(
                id = "agent_coder",
                name = "Programador",
                role = "Implementa el código y la lógica.",
                description = "Escribe código Kotlin limpio, corrutinas, Room DB y arquitectura MVVM sólida.",
                model = "GPT-4o",
                provider = "OpenAI",
                status = AgentStatus.ACTIVO,
                colorHex = 0xFF10B981,
                tasksCompleted = 89
            ),
            CatAgent(
                id = "agent_analyst",
                name = "Analista",
                role = "Revisa, prueba y valida resultados.",
                description = "Auditoría de código, detección de cuellos de botella, cobertura de tests y QA.",
                model = "DeepSeek R1",
                provider = "Local",
                status = AgentStatus.EN_ESPERA,
                colorHex = 0xFFF59E0B,
                tasksCompleted = 28
            ),
            CatAgent(
                id = "agent_researcher",
                name = "Investigador",
                role = "Busca información y referencias.",
                description = "Investigación de bibliotecas existentes (Reuse before rebuild) y documentación técnica.",
                model = "Gemini 1.5 Flash",
                provider = "Google",
                status = AgentStatus.ACTIVO,
                colorHex = 0xFF06B6D4,
                tasksCompleted = 54
            ),
            CatAgent(
                id = "agent_custom",
                name = "Personalizado",
                role = "Agrega tus propios agentes.",
                description = "Crea agentes a medida con system instructions y permisos personalizados.",
                model = "Configurable",
                provider = "A elección",
                status = AgentStatus.ACTIVO,
                colorHex = 0xFFEC4899,
                tasksCompleted = 0
            )
        )

        val initialFiles = listOf(
            CatFileItem("f1", "Documentos", "/workspace/docs", isDirectory = true, itemCount = 12, lastModified = "Hoy, 10:15"),
            CatFileItem("f2", "Imágenes", "/workspace/assets/images", isDirectory = true, itemCount = 8, lastModified = "Hoy, 11:30"),
            CatFileItem("f3", "Código", "/workspace/src", isDirectory = true, itemCount = 24, lastModified = "Hoy, 14:10"),
            CatFileItem("f4", "Modelos", "/workspace/models", isDirectory = true, itemCount = 6, lastModified = "Ayer, 16:45"),
            CatFileItem("f5", "Recursos", "/workspace/res", isDirectory = true, itemCount = 10, lastModified = "Ayer, 09:20"),
            CatFileItem("f6", "README.md", "/workspace/README.md", isDirectory = false, size = "2.4 KB", lastModified = "hace 2 h", extension = "md"),
            CatFileItem("f7", "config.json", "/workspace/config.json", isDirectory = false, size = "1.2 KB", lastModified = "hace 3 h", extension = "json")
        )

        val initialChat = listOf(
            CatChatMessage(
                id = "m1",
                senderId = "user",
                senderName = "Usuario",
                isUser = true,
                text = "Quiero crear una app de tareas que se sincronice en la nube.",
                timestamp = "09:41"
            ),
            CatChatMessage(
                id = "m2",
                senderId = "agent_architect",
                senderName = "Arquitecto",
                isUser = false,
                text = "Perfecto. Para este proyecto necesito confirmar algunos detalles:\n\n1. ¿Qué plataforma prefieres? (Android, iOS o ambas)\n2. ¿Qué funciones básicas quieres incluir?\n3. ¿Tienes alguna preferencia de tecnología o lenguaje?",
                timestamp = "09:42",
                quickReplies = listOf("Android", "iOS", "Ambas")
            )
        )

        _uiState.update {
            it.copy(
                projects = initialProjects,
                agents = initialAgents,
                files = initialFiles,
                chatMessages = initialChat
            )
        }
    }

    // Navigation
    fun selectTab(tab: CatNavTab) {
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

    // Project filters
    fun setProjectFilter(filter: String) {
        _uiState.update { it.copy(projectFilter = filter) }
    }

    fun setProjectSearchQuery(query: String) {
        _uiState.update { it.copy(projectSearchQuery = query) }
    }

    // File filters
    fun setFileFilter(filter: String) {
        _uiState.update { it.copy(fileFilter = filter) }
    }

    fun setFileSearchQuery(query: String) {
        _uiState.update { it.copy(fileSearchQuery = query) }
    }

    // Chat actions
    fun onChatInputChange(input: String) {
        _uiState.update { it.copy(currentChatInput = input) }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = CatChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = "user",
            senderName = "Usuario",
            isUser = true,
            text = text,
            timestamp = "Ahora"
        )
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                currentChatInput = "",
                isAgentTyping = true
            )
        }

        viewModelScope.launch {
            delay(1200)
            val agentResponse = when {
                text.contains("Android", ignoreCase = true) -> {
                    CatChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = "agent_architect",
                        senderName = "Arquitecto",
                        isUser = false,
                        text = "Excelente elección. Para Android nativo utilizaremos Kotlin, Jetpack Compose y Room DB para sincronización local/offline primero. Diseñador comenzará con los mockups y Programador configurará el esquema de base de datos.",
                        timestamp = "Ahora",
                        quickReplies = listOf("Ver arquitectura", "Comenzar sprint", "Configurar Room")
                    )
                }
                text.contains("Ambas", ignoreCase = true) || text.contains("iOS", ignoreCase = true) -> {
                    CatChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = "agent_architect",
                        senderName = "Arquitecto",
                        isUser = false,
                        text = "Entendido. Diseñaremos una arquitectura desacoplada basada en Clean Architecture con ViewModel compartido para que la lógica de sincronización sea portable.",
                        timestamp = "Ahora",
                        quickReplies = listOf("Aprobar plan", "Revisar dependencias")
                    )
                }
                else -> {
                    CatChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = "agent_architect",
                        senderName = "Arquitecto",
                        isUser = false,
                        text = "Anotado. He actualizado el plan del proyecto con tus especificaciones. ¿Deseas que el Programador comience con la estructura de directorios?",
                        timestamp = "Ahora",
                        quickReplies = listOf("Sí, comenzar", "Ver cambios propuestos")
                    )
                }
            }
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + agentResponse,
                    isAgentTyping = false
                )
            }
        }
    }

    // New Project Flow
    fun onNewProjectNameChange(name: String) {
        _uiState.update { it.copy(newProjectName = name) }
    }

    fun onNewProjectDescriptionChange(desc: String) {
        _uiState.update { it.copy(newProjectDescription = desc) }
    }

    fun onCategoryTemplateSelect(category: ProjectCategory) {
        _uiState.update { it.copy(selectedCategoryTemplate = category) }
    }

    fun onTemplateSelect(template: String) {
        _uiState.update { it.copy(selectedTemplateName = template) }
    }

    fun createProject() {
        val state = _uiState.value
        val name = if (state.newProjectName.isNotBlank()) state.newProjectName else "Nuevo Proyecto"
        val newProj = CatProject(
            id = "proj_${System.currentTimeMillis()}",
            name = name,
            description = if (state.newProjectDescription.isNotBlank()) state.newProjectDescription else "Proyecto generado con plantilla ${state.selectedTemplateName}",
            category = state.selectedCategoryTemplate,
            status = ProjectStatus.EN_PROGRESO,
            lastActivity = "Recién creado",
            progressPercent = 10,
            accentColorHex = 0xFFF5A623,
            filesCount = 5,
            tasks = listOf(
                ProjectTask("t1", "Inicialización de repositorio", TaskStatus.COMPLETADA, "Arquitecto"),
                ProjectTask("t2", "Configuración de agentes", TaskStatus.EN_PROGRESO, "Programador")
            )
        )
        _uiState.update {
            it.copy(
                projects = listOf(newProj) + it.projects,
                selectedProjectId = newProj.id,
                currentRoute = CatScreenRoute.PROJECT_DETAIL,
                newProjectName = "",
                newProjectDescription = "",
                toastMessage = "Proyecto '${newProj.name}' creado exitosamente"
            )
        }
    }

    // Action approval request (Section 12.8)
    fun triggerSampleApprovalRequest() {
        _uiState.update {
            it.copy(
                showApprovalSheet = true,
                pendingApproval = ActionApprovalRequest(
                    id = "act_1",
                    agentName = "Programador",
                    actionTitle = "Modificar 14 archivos de base de datos",
                    reason = "Actualizar esquema de tablas para soportar sincronización en tiempo real y refactorizar repositorios.",
                    affectedFiles = listOf(
                        "app/src/main/java/com/example/model/InventoryEntity.kt (+45 -12)",
                        "app/src/main/java/com/example/data/InventoryDao.kt (+28 -4)",
                        "app/src/main/java/com/example/repository/SyncRepository.kt (+80 -25)",
                        "app/src/main/java/com/example/service/SyncWorker.kt (+110 -0)",
                        "... y 10 archivos de migración adicionales"
                    )
                )
            )
        }
    }

    fun approvePendingAction() {
        _uiState.update {
            it.copy(
                showApprovalSheet = false,
                pendingApproval = null,
                toastMessage = "Acción aprobada. El Programador está aplicando los cambios."
            )
        }
    }

    fun denyPendingAction() {
        _uiState.update {
            it.copy(
                showApprovalSheet = false,
                pendingApproval = null,
                toastMessage = "Acción rechazada por el usuario."
            )
        }
    }

    fun dismissApprovalSheet() {
        _uiState.update { it.copy(showApprovalSheet = false) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun toggleDarkMode() {
        _uiState.update { it.copy(isDarkMode = !it.isDarkMode) }
    }
}
