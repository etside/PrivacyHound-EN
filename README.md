# PrivacyHound <img src="app/src/main/res/drawable/ic_xtq_mascot.jpg" width="128" height="128">

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**PrivacyHound** is a privacy monitoring tool designed for Android users, developed by **HackingGroup**. Just like its namesake (the legendary hound), it keeps a sharp eye on your device, capturing and recording sensitive permission usage by various apps in real-time.

---

## About HackingGroup

**HackingGroup** is dedicated to advancing cybersecurity and privacy protection technologies. We believe in the power of technology to better safeguard personal privacy.

- **Official Website**: [https://hackinggroup.org](https://hackinggroup.org)

---

## Features

- **Real-time Monitoring**: Detect camera, microphone, and location (GPS) hardware usage in real-time.
- **Sensitive Data Tracking**: Record app access to contacts, SMS, and other sensitive data.
- **Privacy Dashboard**: Visual overview of which apps are currently using sensitive permissions and sensors.
- **Usage History**: Detailed log of every permission access event — start time, duration, and calling app.
- **Precise Identification**: Uses Android AppOps statistics to accurately match permission usage records.

---

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture (Repository Pattern)
- **Async**: Kotlin Coroutines & Flow
- **Storage**: Room Database
- **Core Components**: Android Service, AppOpsManager, Accessibility Service (if applicable)

---

## Screenshots

<p align="center">
  <img src="1.jpg" width="30%" alt="Dashboard" />
  <img src="2.jpg" width="30%" alt="History" />
  <img src="3.jpg" width="30%" alt="Guide" />
</p>

---

## Getting Started

### Requirements
- Android Studio Ladybug | 2024.2.1 or later
- JDK 17+
- Android 8.0 (API 26)+

### Build & Run
1. Clone the repository: `git clone https://github.com/your-username/PrivacyHound.git`
2. Open the project in Android Studio.
3. Wait for Gradle sync to complete.
4. Connect an Android device and click **Run**.

---

## Privacy

**Standalone Security**: PrivacyHound is a **standalone application** with no network access permissions. It **does not** upload any of your personal data. All monitoring logs are stored locally on your device in a Room database. This software is fully open-source, and you are welcome to audit the source code.

---

## Disclaimer

This software is for security research and educational purposes only. Please use it in compliance with local laws and regulations.

---

## Contributing

Contributions of any kind are welcome!
- Submit Issues to report bugs.
- Submit Pull Requests to improve code or add features.
- Improve documentation.

---

## License

This project is licensed under the [Apache License 2.0](LICENSE).

Copyright (c) 2024 HackingGroup.
