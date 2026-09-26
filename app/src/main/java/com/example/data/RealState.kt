package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

enum class IssueStatus {
    UNVERIFIED, VERIFIED, REFUTED, FIXED, REGRESSION
}

enum class IssueSeverity {
    BLOCKER, CRITICAL, MAJOR, MINOR, INFO
}

@Entity(tableName = "issues", indices = [Index("projectId"), Index("status")])
data class IssueEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val severity: IssueSeverity,
    val category: String,
    val requirement: String,
    val problem: String,
    val expected: String,
    val actual: String,
    val evidence: String,
    val file: String,
    val line: Int,
    val reproduction: String,
    val status: IssueStatus,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(tableName = "tool_logs", indices = [Index("projectId"), Index("timestampMillis")])
data class ToolLogEntity(
    @PrimaryKey val id: String,
    val projectId: String?,
    val agentName: String,
    val tool: String,
    val arguments: String,
    val outcome: String,
    val detail: String,
    val durationMs: Long,
    val timestampMillis: Long
)

@Entity(tableName = "build_records", indices = [Index("projectId"), Index("startedAtMillis")])
data class BuildRecordEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val command: String,
    val state: String,
    val exitCode: Int?,
    val artifactPath: String?,
    val logTail: String,
    val startedAtMillis: Long,
    val finishedAtMillis: Long?
)

@Dao
interface IssueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(issue: IssueEntity)

    @Query("SELECT * FROM issues WHERE projectId = :projectId ORDER BY createdAtMillis DESC")
    suspend fun forProject(projectId: String): List<IssueEntity>

    @Query("SELECT * FROM issues ORDER BY createdAtMillis DESC")
    suspend fun getAll(): List<IssueEntity>

    @Query("UPDATE issues SET status = :status, updatedAtMillis = :updatedAt WHERE id = :id")
    suspend fun setStatus(id: String, status: IssueStatus, updatedAt: Long)

    @Query("DELETE FROM issues WHERE projectId = :projectId")
    suspend fun deleteForProject(projectId: String)
}

@Dao
interface ToolLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ToolLogEntity)

    @Query("SELECT * FROM tool_logs WHERE projectId = :projectId ORDER BY timestampMillis DESC LIMIT 100")
    suspend fun forProject(projectId: String): List<ToolLogEntity>

    @Query("SELECT * FROM tool_logs ORDER BY timestampMillis DESC LIMIT 200")
    suspend fun recent(): List<ToolLogEntity>
}

@Dao
interface BuildRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: BuildRecordEntity)

    @Query("SELECT * FROM build_records WHERE projectId = :projectId ORDER BY startedAtMillis DESC LIMIT 20")
    suspend fun forProject(projectId: String): List<BuildRecordEntity>

    @Query("SELECT * FROM build_records ORDER BY startedAtMillis DESC LIMIT 50")
    suspend fun recent(): List<BuildRecordEntity>
}
