# Runtime integration decision

## Selected candidate

The preferred runtime candidate is the standalone Android library from https://github.com/oonid/pr, specifically its :proot-engine module. It is a native Android/Kotlin engine with a PTY/session stack and Debian/OCI image support. It avoids a Termux dependency and is a better fit than adopting an LD_PRELOAD runtime whose public binaries have proprietary redistribution restrictions.

## Why this is not vendored yet

The upstream repository documents an NDK r27c and Rust toolchain build, patched PRoot, loader binaries, a separate CLI and Android library. It also documents Android seccomp, SELinux and W^X workarounds. This is a substantial native dependency, not a safe one-file Gradle dependency. The repository credits GPL-2.0 and GPL-3.0 components, so Ghost OS must review license compatibility and preserve notices before redistributing source or binaries.

The upstream documentation describes a modern target SDK but does not establish API 24 compatibility. Ghost OS currently declares minSdk 24. We must test the engine on API 24 before claiming that minimum; if unsupported, make an explicit product decision rather than silently raising minSdk or shipping a broken feature.

## Integration gate

1. Pin the upstream to an immutable commit SHA.
2. Review the upstream LICENSE, submodules, vendored sources and generated binary notices.
3. Build ARM64 native components from source with NDK r27c and the documented Rust target.
4. Integrate only the engine module and required session-core pieces behind Ghost OS's LinuxRuntime abstraction; do not import upstream UI or branding.
5. Keep guest files in Ghost OS app-private storage.
6. Use a pinned Debian stable OCI image digest; verify every manifest and layer digest.
7. Test install, cancellation, recovery, PTY I/O, resize, signals, process cleanup, apt, and app lifecycle on the target Samsung device.
8. Keep arbitrary host-shell execution disabled. PRoot is userspace compatibility, not a VM or hardened security boundary.

## Other planned modules

Package management should use the guest's signed Debian repositories. AI should be provider-neutral, opt-in for cloud providers, and use Android Keystore for secrets. Tool modules must be individually reviewed for license, immutable source integrity, ABI, dependencies and required capabilities. Start with passive diagnostics and authorized OSINT; disclose unsupported root, kernel, raw-socket and Wi-Fi monitor-mode requirements. Desktop support is a later milestone after PTY and session lifecycle are stable.
