package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.model.AgentStatus
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: ProjectEntity)

    @Query("SELECT * FROM projects ORDER BY lastActivityMillis DESC")
    suspend fun getAll(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: String): ProjectEntity?

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    suspend fun forProject(projectId: String): List<TaskEntity>
}

@Dao
interface AgentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(agents: List<AgentEntity>)

    @Query("SELECT * FROM agents ORDER BY id")
    suspend fun getAll(): List<AgentEntity>

    @Query("SELECT * FROM agents WHERE id = :id")
    suspend fun getById(id: String): AgentEntity?

    @Query("SELECT COUNT(*) FROM agents")
    suspend fun count(): Int

    @Query("DELETE FROM agents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE agents SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: AgentStatus)

    @Query("UPDATE agents SET tasksCompleted = tasksCompleted + 1 WHERE id = :id")
    suspend fun incrementTasksCompleted(id: String)
}

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages ORDER BY timestampMillis DESC LIMIT 200")
    suspend fun recent(): List<ChatMessageEntity>
}

class Converters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val stringListAdapter: JsonAdapter<List<String>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))

    @TypeConverter
    fun fromStringList(value: List<String>): String = stringListAdapter.toJson(value)

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList() else stringListAdapter.fromJson(value) ?: emptyList()
}

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        AgentEntity::class,
        ChatMessageEntity::class,
        IssueEntity::class,
        ToolLogEntity::class,
        BuildRecordEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CatDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun agentDao(): AgentDao
    abstract fun chatDao(): ChatDao
    abstract fun issueDao(): IssueDao
    abstract fun toolLogDao(): ToolLogDao
    abstract fun buildRecordDao(): BuildRecordDao

    companion object {
        @Volatile
        private var instance: CatDatabase? = null

        fun get(context: Context): CatDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CatDatabase::class.java,
                    "cat_project_agent.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { instance = it }
            }

        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS issues (id TEXT NOT NULL PRIMARY KEY, projectId TEXT NOT NULL, " +
                        "severity TEXT NOT NULL, category TEXT NOT NULL, requirement TEXT NOT NULL, problem TEXT NOT NULL, " +
                        "expected TEXT NOT NULL, actual TEXT NOT NULL, evidence TEXT NOT NULL, file TEXT NOT NULL, " +
                        "line INTEGER NOT NULL, reproduction TEXT NOT NULL, status TEXT NOT NULL, " +
                        "createdAtMillis INTEGER NOT NULL, updatedAtMillis INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_issues_projectId ON issues(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_issues_status ON issues(status)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS tool_logs (id TEXT NOT NULL PRIMARY KEY, projectId TEXT, " +
                        "agentName TEXT NOT NULL, tool TEXT NOT NULL, arguments TEXT NOT NULL, outcome TEXT NOT NULL, " +
                        "detail TEXT NOT NULL, durationMs INTEGER NOT NULL, timestampMillis INTEGER NOT NULL)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tool_logs_projectId ON tool_logs(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tool_logs_timestampMillis ON tool_logs(timestampMillis)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS build_records (id TEXT NOT NULL PRIMARY KEY, projectId TEXT NOT NULL, " +
                        "command TEXT NOT NULL, state TEXT NOT NULL, exitCode INTEGER, artifactPath TEXT, " +
                        "logTail TEXT NOT NULL, startedAtMillis INTEGER NOT NULL, finishedAtMillis INTEGER)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_build_records_projectId ON build_records(projectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_build_records_startedAtMillis ON build_records(startedAtMillis)")
            }
        }
    }
}
