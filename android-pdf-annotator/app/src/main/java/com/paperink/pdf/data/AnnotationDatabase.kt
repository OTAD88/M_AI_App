package com.paperink.pdf.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "annotations", primaryKeys = ["documentUri", "id"])
data class AnnotationRow(
    val documentUri: String,
    val id: String,
    val pageIndex: Int,
    val payload: String
)

@Entity(tableName = "reading_positions")
data class ReadingPosition(
    @PrimaryKey val documentUri: String,
    val pageIndex: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface AnnotationDao {
    @Query("SELECT * FROM annotations WHERE documentUri = :uri ORDER BY pageIndex, id")
    fun observeDocument(uri: String): Flow<List<AnnotationRow>>

    @Query("SELECT * FROM annotations WHERE documentUri = :uri AND pageIndex = :page")
    suspend fun page(uri: String, page: Int): List<AnnotationRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: AnnotationRow)

    @Query("DELETE FROM annotations WHERE documentUri = :uri AND id = :id")
    suspend fun delete(uri: String, id: String)

    @Query("DELETE FROM annotations WHERE documentUri = :uri")
    suspend fun deleteDocument(uri: String)
}

@Dao
interface ReadingPositionDao {
    @Query("SELECT * FROM reading_positions WHERE documentUri = :uri LIMIT 1")
    suspend fun get(uri: String): ReadingPosition?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(position: ReadingPosition)
}

@Database(entities = [AnnotationRow::class, ReadingPosition::class], version = 1, exportSchema = false)
abstract class AnnotationDatabase : RoomDatabase() {
    abstract fun annotations(): AnnotationDao
    abstract fun positions(): ReadingPositionDao

    companion object {
        @Volatile private var INSTANCE: AnnotationDatabase? = null
        fun get(context: Context): AnnotationDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext, AnnotationDatabase::class.java, "paperink.db"
            ).build().also { INSTANCE = it }
        }
    }
}
