# GetDezz

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Android](https://img.shields.io/badge/Platform-Android%2010%2B%20%28API%2029%2B%29-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%26%20Material%203-4285F4.svg)](https://developer.android.com/jetpack/compose)

**GetDezz** is a free, open-source (GPL-3.0) Android application for downloading video and audio streams on-device from YouTube and hundreds of other supported sites. Powered by on-device `yt-dlp` and `ffmpeg`.

---

## Key Features

- **Share Sheet Integration**: Share any video link from YouTube, browsers, or social apps directly to GetDezz to open an instant download overlay.
- **On-Device Engine**: Runs `yt-dlp` and `ffmpeg` natively on your Android device without third-party servers or proxy APIs.
- **Video & Audio Presets**:
  - **Video**: Up to 4K (2160p), 1080p, 720p, 480p in **MP4**, **MKV**, or **WebM**.
  - **Audio**: High quality extractions in **MP3**, **M4A**, **Opus**, **FLAC**, or **WAV**.
- **Background Downloads**: WorkManager plus Foreground Service notification with live percentage progress, ETA, and a 1-tap **Cancel** action.
- **MediaStore Publishing**: Automatic organized publishing to Android's native `Movies/GetDezz` and `Music/GetDezz` public library.
- **In-App Media Player**: Built-in Media3 ExoPlayer preview player to play completed video and audio downloads directly inside the app.
- **100% Private & Tracker-Free**: No ads, no analytics, no user accounts, no telemetry, no backend server. All processing is strictly local.
- **In-App Engine Updates**: Keep `yt-dlp` up-to-date with one tap inside app settings.

---

## Tech Stack

- **UI Framework**: Jetpack Compose, Material 3, Navigation Compose
- **Database & Storage**: Room Database (with KSP), DataStore Preferences
- **Background Execution**: WorkManager (`CoroutineWorker` + Foreground Service `dataSync`)
- **Engine Core**: `io.github.junkfood02.youtubedl-android` (`library` + `ffmpeg`)
- **Media Playback & Images**: Media3 ExoPlayer, Coil Image Loader
- **Dependency Management**: Manual Dependency Injection via `AppContainer`

---

## Architecture

The app follows standard Android architectural best practices with Room as the single source of truth:

```
com.j0yb0y.getdezz
├── data        # Room Database (DownloadEntity, DownloadDao), Repositories, DataStore Settings
├── engine      # yt-dlp & FFmpeg wrappers, OptionBuilder, StreamInfo parser, update manager
├── storage     # Scoped storage MediaStore publisher (Movies/GetDezz & Music/GetDezz)
├── work        # DownloadWorker, WorkManager scheduler, NotificationHelper
├── share       # ShareReceiverActivity overlay for Android system share sheet
└── ui          # Jetpack Compose screens, bottom sheets, preview player, Material 3 theme
```

---

## Building from Source

### Prerequisites

- Android Studio Ladybug or newer
- JDK 17 or higher
- Android SDK with API 35/37 build tools

### Build Commands

1. **Clone the repository**:

   ```bash
   git clone git@github.com:j0yb0y-m/GetDezz.git
   cd GetDezz
   ```

2. **Run Unit Tests**:

   ```bash
   ./gradlew testDebugUnitTest
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew app:assembleDebug
   ```

---

## License

GetDezz is licensed under the **GNU General Public License v3.0 (GPL-3.0)**. See the [LICENSE](LICENSE) file for details.
