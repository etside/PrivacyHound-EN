# PrivacyHound English Version — Implementation Plan

## Context
PrivacyHound (哮天犬) is a Chinese Android privacy monitoring app by HackingGroup. The user wants an English clone with GitHub Actions CI for building APKs.

## Scope
Translate all user-facing Chinese text to English, create a new GitHub repo, and add a build workflow.

## Changes Required

### 1. Translate `app/src/main/res/values/strings.xml`
All 50+ Chinese string resources → English. Key translations:
- `app_name`: "哮天犬" → "PrivacyHound"
- `app_subtitle`, `dashboard_header_tagline` → English equivalents
- All notification channels, alert text, guide text, nav labels, hardware labels, etc.

### 2. Translate hardcoded Chinese in `HardwareOp.kt` (line 53-58)
`labelForType()` returns hardcoded Chinese: "麦克风", "位置", "通讯录读取", "短信读取", "摄像头"
→ "Microphone", "Location", "Contacts Access", "SMS Access", "Camera"

### 3. Translate Chinese comments in Kotlin files (optional but clean)
Comments in `MonitorService.kt`, `PublicApiHardwareMonitor.kt`, `HardwareOp.kt`, `MonitorConstants.kt`, `AlertOverlayService.kt` — translate to English for consistency.

### 4. Update `README.md`
Make it English-only (remove Chinese sections, keep English content).

### 5. Add GitHub Actions workflow
`.github/workflows/build.yml` — Standard Android CI:
- Trigger on push/PR to main
- Set up JDK 17, run `./gradlew assembleDebug`
- Upload APK as artifact

### 6. Create new GitHub repo & push
Use `gh repo create` to make a new public repo, push the translated code.

## Files to Modify
- `app/src/main/res/values/strings.xml`
- `app/src/main/java/com/privacyhound/android/monitor/HardwareOp.kt`
- `app/src/main/java/com/privacyhound/android/monitor/MonitorService.kt` (comments)
- `app/src/main/java/com/privacyhound/android/monitor/PublicApiHardwareMonitor.kt` (comments)
- `app/src/main/java/com/privacyhound/android/monitor/MonitorConstants.kt` (comments)
- `app/src/main/java/com/privacyhound/android/overlay/AlertOverlayService.kt` (comments)
- `README.md`
- `.github/workflows/build.yml` (new)

## Verification
- `grep -r` for remaining Chinese characters in user-facing strings
- Check GitHub Actions workflow syntax
- Verify the repo is created and code is pushed
