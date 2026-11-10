# Runtime Engine Compatibility Decision

Status: investigation complete; integration is blocked pending an API-24-compatible engine path.

## Decision

Do not add the current `oonid/pr` Android library modules as a direct dependency of Ghost OS yet. The current upstream `android/proot-engine` module declares `minSdk = 28`; `android/session-core` also declares `minSdk = 28`. Ghost OS currently declares `minSdk = 24`. A Gradle dependency with a higher minimum SDK would either fail manifest validation or force Ghost OS to abandon API 24.

Do not lower those upstream minSdk declarations as a cosmetic workaround. That would not prove that the native code, Android APIs, linker behavior, SELinux handling, or PTY implementation actually work on Android 7.

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
- Upstream currently builds with `compileSdk = 37` and Java 17; Ghost OS currently uses AGP 7.3.1, compileSdk 33, Java 11 bytecode, Kotlin 1.7.20, and Compose Compiler 1.3.2. This is not a drop-in Gradle integration.
- Upstream documents Android-specific W^X, SELinux, and seccomp behavior and a PROOT_LOADER mechanism. These behaviors must be tested in Ghost OS's own application process; command success under `run-as` is not sufficient evidence.
- The engine is ARM64-focused. Ghost OS must detect unsupported ABIs and fail clearly rather than attempting to run incompatible binaries.

## Implementation path

1. Keep Ghost OS's `LinuxRuntime` interface as the stable boundary between the UI and execution engine.
2. Build a small host-side runtime contract and tests first: capability detection, rootfs state, install progress, session lifecycle, cancellation, PTY I/O, resize, exit status, and storage accounting.
3. Investigate a dedicated API-24-compatible PRoot implementation/fork for the legacy path. Audit every native syscall workaround and verify on an API 24 emulator/device before claiming support.
4. Treat the current `oonid/pr` engine as a possible API-28+ backend only after a pinned source import, license notices, dependency alignment, ABI packaging, and in-app process tests.
5. If one engine cannot reliably support API 24, keep the app's minimum at 24 with feature gating: older Android versions receive only capabilities that pass tests; Linux runtime availability is reported honestly. Do not misrepresent API 24 as fully supported.
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
- Tests pass on API 24 (legacy backend) and API 28+ (if using the upstream backend), on ARM64.
- License inventory and third-party notices ship with the app.

Until these criteria pass on-device, the UI must label Linux as unavailable/not installed rather than implying a working guest.