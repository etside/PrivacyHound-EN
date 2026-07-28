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

    /** Stop monitoring: mark all still-open sessions as ended so the UI no longer shows "Ongoing". */
    @Query("UPDATE privacy_events SET endTime = :now WHERE endTime IS NULL")
    suspend fun closeAllActiveAt(now: Long): Int

    // ── Stats queries ──────────────────────────────────────────────

    /** Per-app usage counts grouped by appName and opType. */
    @Query(
        """
        SELECT appName, opType, COUNT(*) AS count
        FROM privacy_events
        GROUP BY appName, opType
        ORDER BY count DESC
        """
    )
    fun observeUsageByApp(): Flow<List<AppUsageCount>>

    /** Daily event counts for the last 7 days. */
    @Query(
        """
        SELECT (startTime / 86400000) AS dayEpoch, COUNT(*) AS count
        FROM privacy_events
        WHERE startTime >= :sinceEpoch
        GROUP BY dayEpoch
        ORDER BY dayEpoch ASC
        """
    )
    fun observeDailyStats(sinceEpoch: Long): Flow<List<DailyCount>>

    /** The single app with the most access events. */
    @Query(
        """
        SELECT appName, COUNT(*) AS count
        FROM privacy_events
        GROUP BY appName
        ORDER BY count DESC
        LIMIT 1
        """
    )
    fun observeMostActiveApp(): Flow<MostActiveApp?>

    /** Snapshot of events newer than [since] (for retention cleanup). */
    @Query("SELECT * FROM privacy_events WHERE startTime >= :since ORDER BY startTime DESC")
    suspend fun getEventsSince(since: Long): List<PrivacyEvent>

    /** Delete all events whose startTime is before [before]. */
    @Query("DELETE FROM privacy_events WHERE startTime < :before")
    suspend fun deleteEventsBefore(before: Long): Int

    @Query("DELETE FROM privacy_events")
    suspend fun deleteAll()
}

/** Aggregated per-app usage count. */
data class AppUsageCount(
    val appName: String,
    val opType: Int,
    val count: Int
)

/** Single day event count. [dayEpoch] is the day's epoch millis divided by 86400000. */
data class DailyCount(
    val dayEpoch: Long,
    val count: Int
)

/** The single most-accessed app. */
data class MostActiveApp(
    val appName: String,
    val count: Int
)
