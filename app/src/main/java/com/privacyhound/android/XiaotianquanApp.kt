package com.privacyhound.android

import android.app.Application
import com.privacyhound.android.data.PrivacyDatabase
import com.privacyhound.android.data.PrivacyRepository
import com.privacyhound.android.notification.NotificationHelper

class XiaotianquanApp : Application() {

    val database: PrivacyDatabase by lazy { PrivacyDatabase.getInstance(this) }
    val repository: PrivacyRepository by lazy { PrivacyRepository(database.privacyEventDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }
}
