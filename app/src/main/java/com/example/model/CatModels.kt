package com.example.model

enum class CatNavTab(val label: String) {
    INICIO("Inicio"),
    PROYECTOS("Proyectos"),
    AGENTES("Agentes"),
    ARCHIVOS("Archivos"),
    MAS("Más")
}

enum class ProjectCategory(val label: String) {
    TODO("Todo"),
    APPS("Apps"),
    WEB("Web"),
    JUEGOS("Juegos"),
    OTROS("Otros")
}

enum class ProjectStatus(val label: String) {
    EN_PROGRESO("En progreso"),
    EN_REVISION("En revisión"),
    COMPLETADO("Completado"),
    EN_PAUSA("En pausa")
}

enum class TaskStatus(val label: String) {
    COMPLETADA("Completada"),
    EN_PROGRESO("En progreso"),
    PENDIENTE("Pendiente")
}

enum class AgentStatus(val label: String) {
    ACTIVO("Activo"),
    PENSANDO("Pensando"),
    TRABAJANDO("Trabajando"),
    EN_ESPERA("En espera"),
    REQUIERE_APROBACION("Requiere aprobación"),
    COMPLETADO("Completado"),
    PAUSADO("Pausado"),
    ERROR("Error")
}

data class ProjectTask(
    val id: String,
    val title: String,
    val status: TaskStatus,
    val assignedAgent: String
)

data class CatProject(
    val id: String,
    val name: String,
    val description: String,
    val category: ProjectCategory,
    val status: ProjectStatus,
    val lastActivity: String,
    val progressPercent: Int = 0,
    val tasks: List<ProjectTask> = emptyList(),
    val filesCount: Int = 0,
    val accentColorHex: Long = 0xFF0EA5E9
)

data class CatAgent(
    val id: String,
    val name: String,
    val role: String,
    val description: String,
    val model: String,
    val provider: String,
    val status: AgentStatus,
    val colorHex: Long,
    val tasksCompleted: Int = 0
)

data class CatFileItem(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val itemCount: Int? = null,
    val size: String? = null,
    val lastModified: String,
    val extension: String = ""
)

data class ActionApprovalRequest(
    val id: String,
    val agentName: String,
    val actionTitle: String,
    val reason: String,
    val affectedFiles: List<String>,
    val actionType: String = "MODIFY_FILES"
)

data class CatChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val quickReplies: List<String> = emptyList(),
    val pendingApproval: ActionApprovalRequest? = null
)
