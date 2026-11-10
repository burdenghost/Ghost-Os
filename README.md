# Ghost OS — Mobile Linux Workstation

**Status:** 0.2.0-alpha / modern Android ARM64 target  
**Minimum Android:** 9.0 (API 28)  
**Build toolchain:** Android Gradle Plugin 9.5.0, Gradle 9.6.0, Kotlin 2.4.10, JDK 17, compileSdk 37  
**Runtime direction:** standalone Android host + isolated Debian userspace + PTY + modular tooling + hybrid AI

Ghost OS is an independent Android Linux workstation. It is not based on Termux and does not require Termux. The project now prioritizes current Android devices and a modern native runtime rather than legacy Android support.

> This is still an alpha. The current app has a safe bootstrap terminal and a runtime contract, but a working Debian rootfs, PRoot integration, package manager, hybrid AI provider adapters, and security-tool execution must not be considered complete until implemented and tested on-device.

## Build requirements

- JDK 17
- Android SDK Platform 37 and Build Tools 36.0.0
- Android NDK 28.2.13676358
- Android Gradle Plugin 9.5.0
- Gradle 9.6.0
- Kotlin 2.4.10 with the Compose compiler Gradle plugin
- ARM64 (`arm64-v8a`) device/runtime target

The Gradle distribution is pinned to 9.8.2. The upstream PRoot source is pinned as a Git submodule under `third_party/pr`; initialize nested submodules before building. The first native build currently requires Linux x86_64, Rust stable with the `aarch64-linux-android` target, Android SDK, and NDK r27c. Run `bash scripts/build-runtime.sh`, then build `:app:assembleDebug`. The GitHub Actions workflow performs this native build and publishes the debug APK as an artifact. AndroidIDE on-device builds cannot compile the upstream native toolchain directly; use the CI artifact until an Android-host native build path is implemented.

## Android compatibility

Ghost OS now requires Android 9 / API 28 or newer. This matches the current minimum declared by the selected upstream PRoot engine and session modules. It does not yet mean every Android 9+ device is verified: native PRoot behavior, target-SDK restrictions, SELinux, seccomp, storage, and 16 KB page-size compatibility still require real-device testing.

`targetSdk` remains 35 until the native loader path is validated against newer target levels. `compileSdk` is 37 so the app can build against the current Android platform APIs.

## Project structure

- `app/` — native Android host and Compose interface
- `app/src/main/java/.../core/terminal/` — safe bootstrap command router
- `app/src/main/java/.../core/runtime/` — engine-independent Linux runtime contract
- `docs/` — architecture, roadmap, runtime compatibility, and security model
- `tooling/catalog/` — reviewed tool manifest schema

## Security and licensing

PRoot is not a virtual machine or a hardened security boundary. Android's application sandbox remains the outer boundary. Build native components from pinned source; do not blindly redistribute upstream binaries. Preserve GPL notices and corresponding source obligations for GPL-covered PRoot/pr-cli components. Never fall back to arbitrary host-shell execution. Cloud AI remains opt-in and provider secrets must be protected by Android Keystore-backed encryption.

## Runtime source integration

The pinned upstream engine is `oonid/pr` at commit `7029b1fe8fa84d64339cabe38f9c5be2ea2ae7ac`. Ghost OS includes the upstream `:proot-engine`, `:session-core`, and `:termlib` modules as Gradle projects. The upstream Android application UI is not imported; Ghost OS retains its own interface and runtime boundary. Nested vendor submodules are required for native builds. Review `third_party/pr/LICENSE` and upstream notices before redistribution.

## Current engineering priority

Integrate the upstream ARM64 PRoot engine behind `LinuxRuntime`, pin its source commit, satisfy license obligations, and pass real-process tests for Debian installation, PTY sessions, cancellation, package management, and process cleanup before enabling the Linux-ready state.
