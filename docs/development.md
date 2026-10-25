# Development

## Project Setup

To run the project, open it in the latest Version of Android Studio and build the project as usual.

By default, there is not enough memory configured for gradle. You can fix this by running.

```sh
scripts/gradle_bootstrap.sh
```

This will configure your global `~/.gradle/gradle.properties` to use more memory, depending on your machine.

Example of the generated properties, check the `gradle_bootstrap.sh` script for exact details.

```properties
# Begin: Gradle JVM bootstrap-generated properties
org.gradle.jvmargs=-Dfile.encoding=UTF-8 -XX:+ExitOnOutOfMemoryError -Xms4g -Xmx16g
kotlin.daemon.jvm.options=-Dfile.encoding=UTF-8 -XX:+ExitOnOutOfMemoryError -Xms4g -Xmx16g
# End: Gradle JVM bootstrap-generated properties
```

## Tests

### Unit tests

To run the unit tests, run the following command:

```sh
./gradlew voiceUnitTest
```

### Instrumentation tests

To run the instrumentation tests, run the following command:

```sh
./gradlew voiceDeviceGithubDebugAndroidTest
```

## Developer Menu

The developer menu is hidden by default.

To unlock it:

1. Open `Settings`.
2. Tap the app version entry 13 times.
3. Open the newly visible developer menu from `Settings`.

## Android APK builds

Debug APKs are installable without signing configuration:

```sh
./gradlew :app:assemblePlayDebug
./gradlew :app:assembleGithubDebug
```

The APKs are written to `app/build/outputs/apk/<flavor>/debug/`. The `Android` GitHub Actions workflow runs the
unit tests, builds both debug flavors and uploads the APKs as artifacts.

Release builds need an ignored `keystore.properties` at the repository root (or
`signing/<flavor>/signing.properties`). It must provide `storeFile`, `storePassword`, `keyAlias` and `keyPassword`.
Without it, release packaging fails with an explanatory error before any artifact is produced. Never commit keystores or
passwords.

## Ktlint

Voice uses **Ktlint** to enforce consistent code formatting.

- Check for formatting issues:

```sh
./gradlew lintKotlin
```

- Auto-fix formatting:

```sh
./gradlew formatKotlin
```

- To make commits fail on formatting errors, set up a pre-commit hook:

```sh
echo "./gradlew lintKotlin" > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

## Releasing

Release publishing is intentionally separate from Android development handoffs. Do not upload or promote to Play
without an explicit release request.

With the existing Raven upload credentials, build a signed release after choosing the next version:

```sh
./gradlew :app:assemblePlayRelease :app:bundlePlayRelease \
  -Pvoice.versionName=<next-version> \
  -Pvoice.versionCode=<greater-than-highest-uploaded-code>
```

Verify the APK signature and AAB structure before uploading to the Play Console. Promotion to production is manual.
An APK or AAB built without the upload key is not release-ready.

## Versioning

Version defaults live in `app/build.gradle.kts`. For a Play upload, pass a `voice.versionCode` greater than the highest
code already uploaded; do not rely on the repository default. `voice.versionName` is the user-visible version.

## Listening and rating behavior

Listening time is recorded from monotonic active playback only. Paused, buffering, seeking and idle time are excluded,
background playback counts, and playback speed never multiplies the measured time. Sessions are attributed to local
calendar dates and checkpointed so a process restart does not double count.

A book earns a level completion only after at least 95% of its known duration has genuinely been played. Seeking to the
end earns nothing, replaying does not duplicate coverage, and awarded books stay awarded. Levels use five books per level
and the same calculation in Settings and Profile.

Play builds show at most one rating reminder per calendar month, only on local dates 8–14, after one hour of genuine
listening in that window, and only in the foreground while playback is paused or stopped. "I've already rated" suppresses
future reminders permanently. "Rate Raven" opens the published listing and has a browser fallback. See
[release notes](release-notes-next.md).

## Pages Deployment

The public product site is <https://praiseoyegoke.framer.website/raven>. It is maintained outside this repository.
