# Ghost OS — Mobile Linux Workstation

**Status:** 0.3.0-alpha / PRoot integration in progress  
**Minimum Android:** 9.0 (API 28)  
**Build toolchain:** AGP 9.4.1, Gradle 9.8.2, Kotlin 2.4.21, JDK 17, compileSdk 37  
**Runtime:** pinned ARM64 PRoot engine + Debian stable + native PTY

Ghost OS is an independent Android Linux workstation. It is not based on Termux and does not require Termux. It targets modern Android ARM64 devices.

> The dashboard is now connected to a real Debian installation path and PTY-backed PRoot session adapter. The integration has not yet passed a complete CI build or on-device test, so do not treat Debian execution as verified until the build and acceptance tests pass.

## Build

- JDK 17
- Android SDK Platform 37 and Build Tools 36.0.0
- Android Gradle Plugin 9.4.1 and Gradle 9.8.2
- Kotlin 2.4.21 with the Compose compiler Gradle plugin
- Android NDK r27c for the upstream native PRoot and Rust CLI build
- ARM64 (`arm64-v8a`) target

Initialize the pinned runtime and nested vendor submodules, then build the native binaries on Linux x86_64:

```bash
git submodule update --init --recursive
bash scripts/build-runtime.sh
gradle :app:assembleDebug
```

The GitHub Actions workflow performs this build and uploads `ghost-os-debug-apk`. AndroidIDE/RV2IDE on-device builds require an Android/ARM64-compatible AAPT2 binary and the IDE-installed CMake; see [RV2IDE on-device build notes](docs/rv2ide-on-device-build.md). The upstream native build scripts still target Linux x86_64, so CI remains the supported full native build until an Android-host native build path is available.

## Architecture

- `app/` — Ghost OS native Android host and Compose dashboard
- `app/src/main/java/.../core/runtime/` — `LinuxRuntime` contract, Debian installer adapter, and PTY session wrapper
- `third_party/pr/` — pinned upstream PRoot engine source (submodule)
- `:proot-engine`, `:session-core`, `:termlib` — upstream Android libraries
- `docs/` — architecture, roadmap, compatibility, and security decisions

## Runtime behavior

Debian is installed into app-private storage. `pr-cli` runs through the upstream process runner, and the terminal session uses the native PTY bridge. No arbitrary Android host-shell fallback is provided. PRoot is not a VM or hardened security boundary; Android's app sandbox remains the outer boundary.

## Licensing

The upstream project is multi-license: Android libraries are MIT; patched PRoot is GPL-2.0-or-later; `pr-cli` is GPL-3.0-or-later; vendor submodules have their own notices. Preserve copyright notices and provide corresponding source as required before distributing binaries. Review `third_party/pr/LICENSE` and nested vendor license files.

## Next acceptance gate

Build the native engine and APK in CI; install on a modern Samsung ARM64 device; install Debian; verify `uname -a`, `id`, `pwd`, `apt update`, PTY resize, Ctrl-C, cancellation, process cleanup, app restart persistence, DNS, and 16 KB page-size compatibility. Until those pass, the runtime remains alpha.
