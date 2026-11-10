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
- Upstream currently builds with `compileSdk = 37` and Java 17; Ghost OS has been moved to AGP 9.5.0, Gradle 9.6.0, Kotlin 2.4.10, JDK 17, compileSdk 37, and NDK 28.2.13676358. Integration still requires validating the upstream modules against this exact toolchain.
- Upstream documents Android-specific W^X, SELinux, and seccomp behavior and a PROOT_LOADER mechanism. These behaviors must be tested in Ghost OS's own application process; command success under `run-as` is not sufficient evidence.
- The engine is ARM64-focused. Ghost OS must detect unsupported ABIs and fail clearly rather than attempting to run incompatible binaries.

## Implementation path

1. Keep Ghost OS's `LinuxRuntime` interface as the stable boundary between the UI and execution engine.
2. Build a small host-side runtime contract and tests first: capability detection, rootfs state, install progress, session lifecycle, cancellation, PTY I/O, resize, exit status, and storage accounting.
3. Use the upstream ARM64 engine as the primary runtime candidate, pin an immutable commit, and verify on Android 9+ devices.
4. Treat the current `oonid/pr` engine as a possible API-28+ backend only after a pinned source import, license notices, dependency alignment, ABI packaging, and in-app process tests.
5. Keep the product minimum at API 28. Gate unsupported ABIs and devices with explicit diagnostics; do not claim untested Android versions or hardware are supported.
6. Pin an immutable upstream commit before importing code. Never track upstream `main` directly in a release build.

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