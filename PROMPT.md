# GetDezz — rules for the coding agent

Read this whole file before every task. These rules override your defaults.

## 1. Product

GetDezz is a free, open-source (GPL-3.0) Android app that downloads video and audio from YouTube and other sites and converts them, using yt-dlp and ffmpeg running **on the device**.

Features:

- Paste a link, or use the Android Share sheet from any app. A small overlay (bottom sheet) lets the user pick format and quality, tap **Start**, and the download plus conversion continues in the background.
- Progress bar and Cancel button, both in the app and in the notification.
- Library of finished downloads with **Preview** (in-app player) and **Open** (external app) buttons.
- Formats: video up to 4K (MP4 / MKV / WebM), audio MP3 / M4A / Opus / FLAC / WAV.
- "Background" means WorkManager plus a foreground notification. There is no cloud sync.

## 2. Hard constraints

- minSdk 29 (Android 10). compileSdk/targetSdk: keep what the project template generated.
- Kotlin, Jetpack Compose, Material 3. Dynamic color on Android 12+, a static fallback color scheme on Android 10–11, light and dark themes.
- On-device only: no backend, no accounts, no analytics, no ads, no crash reporters. The only network traffic is yt-dlp downloads, yt-dlp self-updates, and thumbnail loading.
- Fast and responsive: never block the main thread. All engine calls run on `Dispatchers.IO`.
- All user-visible text lives in `strings.xml`. Use `start`/`end`, never `left`/`right`, so RTL layouts work.

## 3. Fixed tech stack (do not substitute, do not add libraries without asking)

- UI: Compose + Material 3, Navigation Compose, ViewModel + StateFlow
- Storage: Room (with KSP) for download history, DataStore Preferences for settings
- Background work: WorkManager (`CoroutineWorker` + `setForeground`)
- Images: Coil. Preview player: Media3 ExoPlayer
- Engine: `io.github.junkfood02.youtubedl-android` modules `library` and `ffmpeg` (optionally `aria2c`), from `mavenCentral()`, version `0.18.1`. If that version does not resolve, try `0.17.4` and tell me.
- No Hilt, Dagger, Koin, Retrofit, or OkHttp. Use manual DI: an `AppContainer` created in `GetDezzApp : Application`.
- Do NOT change the AGP, Gradle, or Kotlin versions the template generated. Add dependencies through `gradle/libs.versions.toml` using the latest stable versions compatible with the existing setup.

## 4. Architecture

Base package = the package in AndroidManifest. Sub-packages:

- `engine` — yt-dlp wrapper, option builder, presets
- `data` — Room entities/DAO/database, repositories, DataStore
- `work` — `DownloadWorker`, notifications, scheduler
- `storage` — publishing finished files to MediaStore
- `share` — `ShareReceiverActivity` (the overlay)
- `ui` — screens, components, theme, navigation

Single source of truth: the Room table `downloads`. The worker writes progress to Room; the UI only observes Room via `Flow`.

## 5. Library facts (checked against the library README; do not contradict)

- Manifest must set `android:extractNativeLibs="true"` on `<application>`.
- Use `abiFilters` in the app's `build.gradle.kts`: `arm64-v8a` while developing; add `armeabi-v7a` and `x86_64` later. Use ABI splits for release builds, because the bundled Python and ffmpeg make the APK large.
- Initialise once, off the main thread: `YoutubeDL.getInstance().init(context)` then `FFmpeg.getInstance().init(context)`. The first run extracts files and is slow. Expose an `engineState` StateFlow (Initializing / Ready / Failed) and make every caller wait for Ready.
- Cancelling: pass a `processId` when executing a request, then call `YoutubeDL.getInstance().destroyProcessById(processId)`.
- Update yt-dlp inside the app with `YoutubeDL.getInstance().updateYoutubeDL(context, UpdateChannel.STABLE)` (the other channel is `NIGHTLY`).
- Stream info: `YoutubeDL.getInstance().getInfo(url)` returns title, thumbnail, duration, uploader and formats.
- **Never guess an API.** Before calling any library method, open its declaration (Ctrl+click) and use the real signature, with named arguments. If a method does not exist, say so and stop. Do not invent a replacement.
- Scoped storage: do NOT pass absolute `/sdcard/...` paths to `-o`. Download into an app-private temp directory (`getExternalFilesDir("tmp")/<jobId>/`), then publish the result with MediaStore.
- **YouTube needs a JavaScript runtime.** Recent yt-dlp needs an external JS runtime (plus its bundled EJS scripts) to fully support YouTube. Without one you get `No supported JavaScript runtime could be found` and missing formats or failures. Other sites are not affected. Never claim YouTube works until it has been tested on a real device with the real log. If a runtime binary exists at `<nativeLibraryDir>/libdeno.so` or `<nativeLibraryDir>/libqjs.so`, add `--js-runtimes deno:<path>` or `--js-runtimes quickjs:<path>` to YouTube requests.

## 6. Download behaviour rules

- Always pass `--no-playlist` unless the user explicitly chose playlist mode.
- Output file: `-o "<tmpDir>/%(id)s.%(ext)s"`. After the run, take the largest file in `<tmpDir>` that does not end in `.part`, `.ytdl`, `.json`, `.temp`, or a thumbnail extension. That is the result.
- Video presets use `-f "bv*+ba/b"` with `-S "res:<H>,ext:mp4:m4a"` and `--merge-output-format mp4/mkv` (pick the container from the user's choice).
- Audio presets use `-x --audio-format <fmt>`. `--embed-metadata` and `--embed-thumbnail` are best effort: if the run fails because of embedding, retry once without them instead of failing the job.
- Progress reaches 100% more than once when audio and video are fetched separately. When the output line contains `[Merger]`, `[ExtractAudio]`, `[VideoConvertor]` or `[FixupM4a]`, switch the job status to PROCESSING and show an indeterminate bar.
- Throttle DB writes to at most one every 500 ms, and notification updates to at most one per second.
- On cancel or failure always delete the temp directory (inside `withContext(NonCancellable)`).
- Max parallel downloads: default 2, configurable 1–4, using a process-wide `Semaphore`. Waiting jobs show QUEUED.
- Foreground work: `ForegroundInfo` with `ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC`. Manifest needs the `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_DATA_SYNC` and `POST_NOTIFICATIONS` permissions, and the WorkManager `SystemForegroundService` entry with `android:foregroundServiceType="dataSync"` and `tools:node="merge"`. Ask for the notification permission at runtime on API 33+.
- Publish results with MediaStore: video → `Movies/GetDezz`, audio → `Music/GetDezz`. Set `RELATIVE_PATH`, `IS_PENDING=1` while writing and `0` after. Pick the MIME type from the file extension (`.opus` → `audio/ogg`).

## 7. How you must work

1. Do **one phase per request**. Never start work that was not asked for in the current message.
2. Finished means complete: no `TODO`, no pseudo-code, no "implement later", no placeholder screens. Every file you write is whole and compiles.
3. After writing code, run a Gradle build and fix every compile error. For logic that does not need Android, add JVM unit tests and run `./gradlew test`.
4. If you hit the same error twice, **stop** and show me the exact error text and the file. Do not try a third random fix. Do not delete features to make the build pass.
5. Do not rename or move existing files unless the task says so. Do not rewrite files you were not asked to touch.
6. Do not silently choose between options. If something in the task is ambiguous or impossible, say so in one short question.
7. Do not generate a LICENSE file's text. I will add it myself.

## 8. End of every task, reply in this format

1. **Files created/changed** (full paths)
2. **How I test it** (3–6 numbered steps on a device or emulator)
3. **Known limitations or things you could not verify**

## 9. Definition of done (every phase)

- The project builds with no errors and no new warnings about missing declarations.
- The feature works on a real Android 10+ device or emulator by following your own test steps.
- No crashes on rotate, on process death, or when the network is off.
