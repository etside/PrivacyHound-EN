package com.privacyhound.android.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter

class PrivacyRepository(private val dao: PrivacyEventDao) {

    fun observeActive(): Flow<List<PrivacyEvent>> = dao.observeActive()

    fun observeRecent(limit: Int = 5): Flow<List<PrivacyEvent>> = dao.observeRecent(limit)

    fun observeFiltered(query: String?, opType: Int?): Flow<List<PrivacyEvent>> {
        val q = query?.trim().orEmpty()
        val filter = opType ?: -1
        return dao.observeFiltered(q, filter)
    }

    suspend fun insert(event: PrivacyEvent): Long = dao.insert(event)

    suspend fun update(event: PrivacyEvent) = dao.update(event)

    suspend fun getById(id: Long): PrivacyEvent? = dao.getById(id)

    suspend fun closeAllActiveAt(now: Long): Int = dao.closeAllActiveAt(now)

    suspend fun exportCsv(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { w ->
                    w.appendLine("id,packageName,appName,opType,startTime,endTime,isForeground")
                    dao.getAllSnapshot().forEach { e ->
                        w.appendLine(
                            listOf(
                                e.id,
                                csvEscape(e.packageName),
                                csvEscape(e.appName),
                                e.opType,
                                e.startTime,
                                e.endTime ?: "",
                                e.isForeground
                            ).joinToString(",")
                        )
                    }
                }
            } ?: error("openOutputStream failed")
        }
    }

    suspend fun exportJson(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val arr = JSONArray()
            dao.getAllSnapshot().forEach { e ->
                arr.put(
                    JSONObject().apply {
                        put("id", e.id)
                        put("packageName", e.packageName)
                        put("appName", e.appName)
                        put("opType", e.opType)
                        put("startTime", e.startTime)
                        put("endTime", e.endTime ?: JSONObject.NULL)
                        put("isForeground", e.isForeground)
                    }
                )
            }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { it.write(arr.toString(2)) }
            } ?: error("openOutputStream failed")
        }
    }

    private fun csvEscape(s: String): String {
        val needs = s.contains(',') || s.contains('"') || s.contains('\n')
        return if (!needs) s else '"' + s.replace("\"", "\"\"") + '"'
    }

    // ── Stats / analytics ──────────────────────────────────────────

    fun observeUsageByApp(): Flow<List<AppUsageCount>> = dao.observeUsageByApp()

    fun observeDailyStats(): Flow<List<DailyCount>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val sinceEpoch = sevenDaysAgo / (24 * 60 * 60 * 1000)
        return dao.observeDailyStats(sinceEpoch)
    }

    fun observeMostActiveApp(): Flow<MostActiveApp?> = dao.observeMostActiveApp()

    suspend fun deleteAll(): Unit = dao.deleteAll()

    /** Delete events older than [retentionDays] days. Returns the number of rows deleted. */
    suspend fun cleanupOldData(retentionDays: Int = 30): Int = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - retentionDays.toLong() * 24 * 60 * 60 * 1000
        dao.deleteEventsBefore(cutoff)
    }

    /** Lightweight aggregate summary used by the Stats screen. */
    suspend fun getUsageSummary(): UsageSummary = withContext(Dispatchers.IO) {
        val all = dao.getAllSnapshot()
        val now = System.currentTimeMillis()
        val todayStart = now - (now % (24 * 60 * 60 * 1000))
        val yesterdayStart = todayStart - 24L * 60 * 60 * 1000

        val todayCount = all.count { it.startTime >= todayStart }
        val yesterdayCount = all.count { it.startTime in yesterdayStart until todayStart }

        val bySensor = all.groupBy { it.opType }.mapValues { it.value.size }

        UsageSummary(
            totalEvents = all.size,
            todayCount = todayCount,
            yesterdayCount = yesterdayCount,
            bySensor = bySensor
        )
    }
}

/** Pre-computed stats snapshot returned by [PrivacyRepository.getUsageSummary]. */
data class UsageSummary(
    val totalEvents: Int,
    val todayCount: Int,
    val yesterdayCount: Int,
    /** opType -> count */
    val bySensor: Map<Int, Int>
)
