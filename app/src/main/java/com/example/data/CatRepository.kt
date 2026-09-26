package com.example.data

import android.content.Context
import com.example.ai.GeminiClient
import com.example.model.AgentStatus
import com.example.model.CatAgent
import com.example.model.CatChatMessage
import com.example.model.CatProject
import com.example.model.TaskStatus

/**
 * Capa de persistencia: Room como fuente de verdad y mapeo hacia los modelos
 * que consume la UI. Los catálogos (equipo de agentes) se siembran una sola
 * vez; todo lo demás lo genera el uso real de la app.
 */
class CatRepository(context: Context) {

    private val appContext = context.applicationContext
    private val db = CatDatabase.get(appContext)
    private val projectDao = db.projectDao()
    private val taskDao = db.taskDao()
    private val agentDao = db.agentDao()
    private val chatDao = db.chatDao()

    suspend fun seedDefaultAgents() {
        if (agentDao.count() == 0) agentDao.upsertAll(DEFAULT_AGENTS.map { it.toEntity() })
    }

    suspend fun loadProjects(): List<CatProject> {
        val tasksByProject = taskDao.getAll().groupBy { it.projectId }
        return projectDao.getAll().map { entity ->
            val tasks = tasksByProject[entity.id].orEmpty().map { it.toDomain() }
            CatProject(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                category = entity.category,
                status = entity.status,
                lastActivity = formatRelativeActivity(entity.lastActivityMillis),
                progressPercent = progressOf(tasks),
                tasks = tasks,
                filesCount = WorkspaceRepository.countProjectFiles(appContext, entity.id),
                accentColorHex = entity.accentColorHex
            )
        }
    }

    suspend fun insertProject(project: CatProject) {
        val now = System.currentTimeMillis()
        projectDao.upsert(
            ProjectEntity(
                id = project.id,
                name = project.name,
                description = project.description,
                category = project.category,
                status = project.status,
                createdAtMillis = now,
                lastActivityMillis = now,
                accentColorHex = project.accentColorHex
            )
        )
        if (project.tasks.isNotEmpty()) {
            taskDao.insertAll(
                project.tasks.map { TaskEntity(it.id, project.id, it.title, it.status, it.assignedAgent) }
            )
        }
    }

    suspend fun loadAgents(): List<CatAgent> = agentDao.getAll().map { it.toDomain() }

    suspend fun getAgent(id: String?): CatAgent? = id?.let { agentDao.getById(it) }?.toDomain()

    suspend fun findAgentByName(name: String): CatAgent? =
        loadAgents().firstOrNull { it.name.equals(name, ignoreCase = true) }

    suspend fun setAgentStatus(id: String, status: AgentStatus) = agentDao.setStatus(id, status)

    suspend fun incrementAgentTasks(id: String) = agentDao.incrementTasksCompleted(id)

    suspend fun loadChat(): List<CatChatMessage> =
        chatDao.recent().map { it.toDomain() }.reversed()

    suspend fun insertChatMessage(message: CatChatMessage) {
        chatDao.insert(
            ChatMessageEntity(
                id = message.id,
                senderId = message.senderId,
                senderName = message.senderName,
                isUser = message.isUser,
                text = message.text,
                timestampMillis = System.currentTimeMillis(),
                quickReplies = message.quickReplies
            )
        )
    }

    private fun progressOf(tasks: List<com.example.model.ProjectTask>): Int {
        if (tasks.isEmpty()) return 0
        val completed = tasks.count { it.status == TaskStatus.COMPLETADA }
        return completed * 100 / tasks.size
    }

    private fun AgentEntity.toDomain() = CatAgent(
        id = id,
        name = name,
        role = role,
        description = description,
        model = model,
        provider = provider,
        status = status,
        colorHex = colorHex,
        tasksCompleted = tasksCompleted
    )

    private fun CatAgent.toEntity() = AgentEntity(
        id = id,
        name = name,
        role = role,
        description = description,
        model = model,
        provider = provider,
        status = status,
        colorHex = colorHex,
        tasksCompleted = tasksCompleted
    )

    private fun TaskEntity.toDomain() = com.example.model.ProjectTask(
        id = id,
        title = title,
        status = status,
        assignedAgent = assignedAgent
    )

    private fun ChatMessageEntity.toDomain() = CatChatMessage(
        id = id,
        senderId = senderId,
        senderName = senderName,
        isUser = isUser,
        text = text,
        timestamp = formatChatTimestamp(timestampMillis),
        quickReplies = quickReplies
    )
}

private val DEFAULT_AGENTS = listOf(
    CatAgent(
        id = "agent_architect",
        name = "Arquitecto",
        role = "Define la estructura y plan del proyecto.",
        description = "Especialista en patrones de diseño, escalabilidad y diagramas de arquitectura.",
        model = GeminiClient.DEFAULT_MODEL,
        provider = "Google",
        status = AgentStatus.ACTIVO,
        colorHex = 0xFF0EA5E9,
        tasksCompleted = 0
    ),
    CatAgent(
        id = "agent_designer",
        name = "Diseñador",
        role = "Crea la interfaz y experiencia visual.",
        description = "Diseño de sistemas de color, componentes Compose, tokens de espaciado y animaciones.",
        model = GeminiClient.DEFAULT_MODEL,
        provider = "Google",
        status = AgentStatus.ACTIVO,
        colorHex = 0xFFA855F7,
        tasksCompleted = 0
    ),
    CatAgent(
        id = "agent_coder",
        name = "Programador",
        role = "Implementa el código y la lógica.",
        description = "Escribe código Kotlin limpio, corrutinas, Room DB y arquitectura MVVM sólida.",
        model = GeminiClient.DEFAULT_MODEL,
        provider = "Google",
        status = AgentStatus.ACTIVO,
        colorHex = 0xFF10B981,
        tasksCompleted = 0
    ),
    CatAgent(
        id = "agent_analyst",
        name = "Analista",
        role = "Revisa, prueba y valida resultados.",
        description = "Auditoría de código, detección de cuellos de botella, cobertura de tests y QA.",
        model = GeminiClient.DEFAULT_MODEL,
        provider = "Google",
        status = AgentStatus.ACTIVO,
        colorHex = 0xFFF59E0B,
        tasksCompleted = 0
    ),
    CatAgent(
        id = "agent_researcher",
        name = "Investigador",
        role = "Busca información y referencias.",
        description = "Investigación de bibliotecas existentes (Reuse before rebuild) y documentación técnica.",
        model = GeminiClient.DEFAULT_MODEL,
        provider = "Google",
        status = AgentStatus.ACTIVO,
        colorHex = 0xFF06B6D4,
        tasksCompleted = 0
    ),
    CatAgent(
        id = "agent_custom",
        name = "Personalizado",
        role = "Agrega tus propios agentes.",
        description = "Crea agentes a medida con system instructions y permisos personalizados.",
        model = GeminiClient.DEFAULT_MODEL,
        provider = "Google",
        status = AgentStatus.ACTIVO,
        colorHex = 0xFFEC4899,
        tasksCompleted = 0
    )
)
