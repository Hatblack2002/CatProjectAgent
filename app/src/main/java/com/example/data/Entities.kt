package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.model.AgentStatus
import com.example.model.ProjectCategory
import com.example.model.ProjectStatus
import com.example.model.TaskStatus

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: ProjectCategory,
    val status: ProjectStatus,
    val createdAtMillis: Long,
    val lastActivityMillis: Long,
    val accentColorHex: Long
)

@Entity(tableName = "tasks", indices = [Index("projectId")])
data class TaskEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val title: String,
    val status: TaskStatus,
    val assignedAgent: String
)

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val description: String,
    val model: String,
    val provider: String,
    val status: AgentStatus,
    val colorHex: Long,
    val tasksCompleted: Int
)

@Entity(tableName = "chat_messages", indices = [Index("timestampMillis")])
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val senderName: String,
    val isUser: Boolean,
    val text: String,
    val timestampMillis: Long,
    val quickReplies: List<String>
)
