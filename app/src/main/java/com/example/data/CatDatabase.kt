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
    @Insert
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
    entities = [ProjectEntity::class, TaskEntity::class, AgentEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CatDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun agentDao(): AgentDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var instance: CatDatabase? = null

        fun get(context: Context): CatDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CatDatabase::class.java,
                    "cat_project_agent.db"
                ).build().also { instance = it }
            }
    }
}
