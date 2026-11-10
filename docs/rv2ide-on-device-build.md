# RV2IDE on-device build notes

These settings are specific to RV2IDE on Android and must not be copied blindly to desktop Linux builds.

## SDK and native tools

- Android SDK Platform: 37 (the app module's dependencies require compileSdk 37).
- Android NDK: 28.2.13676358.
- CMake: 4.1.2 (select the IDE-installed version).
- Android minimum SDK: 28. Do not lower minSdk.

The three native Android modules in the PRoot source tree pin CMake in their Gradle build scripts. For the RV2IDE working copy, use CMake 4.1.2 consistently in:
- `third_party/pr/android/proot-engine/build.gradle.kts`
- `third_party/pr/android/termlib/build.gradle.kts`
- `third_party/pr/vendor/termlib/lib/build.gradle.kts`

These files are inside the `third_party/pr` Git submodule. A change made inside that directory belongs to the submodule repository/commit, not the Ghost-Os superproject. Preserve and commit the submodule change in its own repository, then update the submodule pointer in Ghost-Os.

## RV2IDE AAPT2

The Maven AAPT2 artifact and SDK Build Tools AAPT2 binaries observed in this environment are Linux x86-64 executables and cannot run directly on Android/ARM64. RV2IDE provides an ARM64 executable at:

```text
$HOME/.androidide/aapt2
```

Add the following machine-specific property to the local Ghost-Os `gradle.properties` (or the user's Gradle properties):

```properties
android.aapt2FromMavenOverride=/data/data/com.tom.rv2ide/files/home/.androidide/aapt2
```

Do not commit this absolute path to the shared repository: it is tied to one RV2IDE installation and will break builds on other machines. The binary's ELF header was verified as AArch64 (`e_machine = 0x00b7`).

## Build command

```bash
./gradlew --no-daemon clean assembleDebug
```

A successful resource-processing step confirms AAPT2 is running; it does not by itself verify the native PRoot runtime or a complete APK build.

## Debian OCI download diagnostics

The PRoot source is a Git submodule, so Ghost-Os cannot directly commit edits to its upstream source repository. The helper script in this repository applies the detailed Rust error formatting locally to the submodule source. Run it from the Ghost-Os repository root after initializing the submodule:

```bash
git submodule update --init --recursive
bash scripts/patch-pr-oci-diagnostics.sh
```

Then rebuild the app and retry Debian installation. This change exposes the underlying transport error chain for Docker Hub blob downloads; it is diagnostic only and does **not** itself fix a network, TLS, DNS, or registry failure.
