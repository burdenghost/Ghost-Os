# Ghost OS roadmap

## Milestone 0 — Modern Android bootstrap
- [x] Native Android project skeleton and Compose dashboard
- [x] Safe bootstrap command router
- [x] Minimum Android 9 / API 28 and ARM64-only target
- [x] Modern Android/Kotlin/Gradle toolchain aligned with upstream runtime
- [x] Pin upstream PRoot engine as a Git submodule
- [x] Include upstream engine, session, and terminal libraries as Gradle projects
- [x] Add CI workflow for native runtime build and debug APK artifact

## Milestone 1 — Linux runtime foundation
- [x] Android host implementing the upstream `ProotHost` contract
- [x] App-private PRoot prefix bootstrap and native executable links
- [x] Debian stable install adapter using upstream `pr-cli` process runner
- [x] PTY session adapter exposed through `LinuxRuntime`
- [x] Dashboard install/start/stop actions connected to runtime adapter
- [ ] Obtain first successful CI build with native PRoot binaries
- [ ] Install Debian and verify guest commands on a real Android 9+ ARM64 device
- [ ] Test `apt update`, DNS, process cancellation, PTY resize, Ctrl-C, and cleanup
- [ ] Validate target-SDK 35 restrictions and 16 KB page-size support

## Milestone 2 — Interactive workstation
- [ ] Multi-session lifecycle and persistent background sessions
- [ ] Guest file browser and explicit shared-storage bridge
- [ ] Package manager UI, repository trust and rollback
- [ ] Editor, Git, SSH, Python and compiler toolchains
- [ ] Backup, reset and update flows

## Deferred — AI integration
AI features are intentionally excluded from the current build. No AI broker, provider adapters, model downloads, or AI command execution will be added until the Linux workstation foundation is stable.

## Milestone 3 — Security and OSINT
- [ ] Tool manifest review and source/license verification
- [ ] OSINT and defensive network modules
- [ ] Compatibility review for Nmap and Metasploit
- [ ] Lab-only workflows, scope confirmation and audit logging
- [ ] Honest labels for root/kernel/USB/Wi-Fi monitor-mode requirements

## Milestone 4 — Release engineering
- [ ] Signed release builds, SBOM and third-party notices
- [ ] Android 9+ device matrix and 16 KB page-size testing
- [ ] Accessibility, localization, crash and performance testing
