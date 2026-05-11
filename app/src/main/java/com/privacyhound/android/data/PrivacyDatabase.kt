package com.privacyhound.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PrivacyEvent::class],
    version = 1,
    exportSchema = false
)
abstract class PrivacyDatabase : RoomDatabase() {

    abstract fun privacyEventDao(): PrivacyEventDao

    companion object {
        @Volatile
        private var INSTANCE: PrivacyDatabase? = null

        fun getInstance(context: Context): PrivacyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PrivacyDatabase::class.java,
                    "privacy_hound.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
