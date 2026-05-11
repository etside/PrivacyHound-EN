package com.privacyhound.android.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "privacy_events",
    indices = [Index(value = ["startTime"]), Index(value = ["packageName"])]
)
data class PrivacyEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    /** 0:camera, 1:mic, 2:gps, 3:contacts read, 4:sms read */
    val opType: Int,
    val startTime: Long,
    val endTime: Long?,
    val isForeground: Boolean
)
