# Runtime Engine Compatibility Decision

Status: API-24 compatibility is intentionally dropped; modern Android ARM64 is now the project target.

## Decision

Ghost OS now targets Android 9 / API 28 and newer. This matches the current upstream `oonid/pr` `proot-engine` and `session-core` minimum SDK declarations. Direct integration is still gated on toolchain alignment, source pinning, licensing, ABI packaging, and runtime tests.

Do not lower upstream minimum SDK declarations as a cosmetic workaround. Android 9+ is the supported product baseline; older Android versions are out of scope.

## Upstream candidate and evidence

- Repository: https://github.com/oonid/pr
- Inspected upstream commit: `7029b1fe8fa84d64339cabe38f9c5be2ea2ae7ac` (2026-11-09).
- Engine module: `android/proot-engine`.
- Session module: `android/session-core`.
- The engine exposes `ProotHost`, `ProotLauncher`, and `PtyNative`, including PTY sessions, command execution, resize, and child-process reaping.
- Upstream build requires Java 17, NDK r27c, and Rust `aarch64-linux-android`; native binaries are built from source.
- The upstream project is multi-license: Android code is MIT; patched PRoot is GPL-2.0-or-later; pr-cli is GPL-3.0-or-later; vendor components have their own notices. Preserve source notices and publish corresponding source for any distributed GPL-covered binaries.

## Android compatibility blockers

- Both `proot-engine` and `session-core` currently declare `minSdk = 28`.
- Upstream currently builds with `compileSdk = 37` and Java 17; Ghost OS has been moved to AGP 9.4.1, Gradle 9.8.2, Kotlin 2.4.21, JDK 17, compileSdk 37, and upstream NDK r27c. Integration still requires validating the upstream modules against this exact toolchain.
- Upstream documents Android-specific W^X, SELinux, and seccomp behavior and a PROOT_LOADER mechanism. These behaviors must be tested in Ghost OS's own application process; command success under `run-as` is not sufficient evidence.
- The engine is ARM64-focused. Ghost OS must detect unsupported ABIs and fail clearly rather than attempting to run incompatible binaries.

## Implementation path

1. Keep Ghost OS's `LinuxRuntime` interface as the stable boundary between the UI and execution engine.
2. Pin the upstream ARM64 engine at an immutable commit and initialize all nested vendor submodules.
3. Build the upstream native PRoot, loader, and Rust `pr-cli` binaries from source; the GitHub Actions workflow and `scripts/build-runtime.sh` now define this build path.
4. Include upstream engine/session/terminal libraries as Gradle projects and keep Ghost OS's own UI.
5. Use the Ghost OS adapter to install Debian into app-private storage and expose a PTY-backed session.
6. Validate the full build and runtime on Android 9+ ARM64 devices before calling the guest production-ready.

## Security constraints

- PRoot is a user-space compatibility layer, not a VM or a hardened security boundary.
- Keep the Linux rootfs inside app-private storage; never execute downloaded binaries from shared storage.
- Do not expose arbitrary host-shell execution through the terminal command router.
- Require explicit authorization and scope limits for OSINT/security modules. Keep destructive or intrusive actions disabled by default.
- Cloud AI remains opt-in; store provider secrets with Android Keystore-backed encryption and never write secrets to logs.

## Acceptance criteria before calling Debian functional

- Fresh Debian rootfs install succeeds and survives app restart.
- `uname -a`, `id`, `pwd`, shell built-ins, and filesystem operations run inside the guest.
- `apt update` and installation of a small harmless package succeed on the app's real process context.
- PTY input/output, terminal resize, Ctrl-C, process exit, cancellation, and cleanup work without leaked child processes.
- Network DNS follows the active Android network.
- Tests pass on Android 9+ ARM64, including at least one modern Samsung device and a 16 KB page-size environment.
- License inventory and third-party notices ship with the app.

Until these criteria pass on-device, the UI must label Linux as unavailable/not installed rather than implying a working guest.