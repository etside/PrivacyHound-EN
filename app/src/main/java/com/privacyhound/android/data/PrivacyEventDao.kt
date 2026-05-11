package com.privacyhound.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacyEventDao {

    @Insert
    suspend fun insert(event: PrivacyEvent): Long

    @Update
    suspend fun update(event: PrivacyEvent)

    @Query(
        """
        SELECT * FROM privacy_events 
        WHERE endTime IS NULL 
        ORDER BY startTime DESC
        """
    )
    fun observeActive(): Flow<List<PrivacyEvent>>

    @Query(
        """
        SELECT * FROM privacy_events 
        ORDER BY startTime DESC 
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<PrivacyEvent>>

    /** [opFilter] 使用 -1 表示不过滤硬件类型 */
    @Query(
        """
        SELECT * FROM privacy_events 
        WHERE (:opFilter = -1 OR opType = :opFilter)
          AND (
            :query = ''
            OR appName LIKE '%' || :query || '%'
            OR packageName LIKE '%' || :query || '%'
          )
        ORDER BY startTime DESC
        """
    )
    fun observeFiltered(query: String, opFilter: Int): Flow<List<PrivacyEvent>>

    @Query("SELECT * FROM privacy_events ORDER BY startTime DESC")
    suspend fun getAllSnapshot(): List<PrivacyEvent>

    @Query("SELECT * FROM privacy_events WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PrivacyEvent?

    /** 停止监测时将未结束的会话标记结束，避免界面长期显示「使用中 / 进行中」 */
    @Query("UPDATE privacy_events SET endTime = :now WHERE endTime IS NULL")
    suspend fun closeAllActiveAt(now: Long): Int
}
