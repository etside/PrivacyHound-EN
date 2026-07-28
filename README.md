# eT_Saftey_Manager_Premium <img src="app/src/main/res/drawable/ic_xtq_mascot.jpg" width="128" height="128">

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**eT_Saftey_Manager_Premium** is a premium privacy monitoring tool for Android. Originally based on PrivacyHound by HackingGroup, this version has been significantly enhanced with a new premium tier system, gold/charcoal design, and admin management capabilities.

Developed and maintained by **EngineersTech**.

---

## What's New (Premium Upgrade)

- **Premium Tier System**: Gold and Platinum tiers with feature gating
- **License Verification**: HMAC-SHA256 passphrase activation
- **Premium UI**: Gold/charcoal design with shimmer animations
- **Admin Panel**: Standalone admin app for user management
- **Settings Screen**: Full control over monitoring, alerts, and appearance
- **Statistics Dashboard**: Usage analytics with charts and trends
- **Dark Mode**: System/Light/Dark theme support
- **Performance**: Optimized polling, reduced battery usage

---

## Features

- **Real-time Monitoring**: Detect camera, microphone, location, contacts, and SMS access
- **Privacy Dashboard**: Live overview of which apps are using sensitive sensors
- **Usage History**: Detailed log of every permission access event
- **Statistics**: Top offenders, daily activity, sensor breakdown, weekly trends
- **Overlay Alerts**: Floating alert bar when apps access hardware
- **Precise Mode**: AppOps-based identification via ADB authorization
- **Premium Features**: Location tracking, SMS/Contacts, data export, unlimited history

---

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: Repository Pattern
- **Async**: Kotlin Coroutines & Flow
- **Storage**: Room Database
- **CI/CD**: GitHub Actions

---

## Getting Started

### Requirements
- Android Studio Ladybug | 2024.2.1 or later
- JDK 17+
- Android 8.0 (API 26)+

### Build & Run
1. Clone the repository: `git clone https://github.com/etside/PrivacyHound-EN.git`
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Connect an Android device and click **Run**

### Premium Activation
1. Open the app and tap the premium icon
2. Contact us on WhatsApp at +8801873722228
3. Provide your email or phone number
4. Receive a passphrase from the admin
5. Enter the passphrase in the app to activate

---

## Privacy

**Standalone Security**: eT_Saftey_Manager_Premium is a **standalone application** with no network access permissions. It **does not** upload any of your personal data. All monitoring logs are stored locally on your device in a Room database.

---

## Disclaimer

This software is for security research and educational purposes only. Please use it in compliance with local laws and regulations.

---

## Credits

- Original project: [PrivacyHound](https://github.com/iHackingGroup/PrivacyHound) by **HackingGroup**
- Premium upgrade and maintenance: **EngineersTech**

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

No copyright restrictions apply to this repository.
