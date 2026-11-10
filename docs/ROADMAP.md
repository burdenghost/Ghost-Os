# Ghost OS roadmap

## Milestone 0 — Bootstrap alpha
- [x] Native Android project skeleton and Compose dashboard
- [x] Safe built-in terminal prototype
- [x] Android 7 / API 24 minimum target
- [x] Successful on-device debug build reported by project owner
- [x] Kotlin 1.7.20 + Compose Compiler 1.3.2 compatibility correction
- [x] Import alpha source tree into this repository
- [ ] Gradle wrapper and CI build

## Milestone 1 — Linux runtime foundation
- [ ] Select and license-review a maintained ARM64 rootless runtime engine
- [ ] Add verified Debian rootfs manifest and downloader
- [ ] Safe extraction, disk checks, install progress, cancellation, recovery
- [ ] Runtime lifecycle and status persistence
- [ ] API/device compatibility tests

## Milestone 2 — Interactive workstation
- [ ] PTY-backed multi-session terminal
- [ ] Guest file browser and explicit shared-storage bridge
- [ ] Package manager, repository trust and rollback
- [ ] Editor, Git, SSH, Python and compiler toolchains
- [ ] Backup, reset and update flows

## Milestone 3 — Hybrid AI
- [ ] Offline help and diagnostics engine
- [ ] Provider-neutral adapter interface
- [ ] Optional cloud provider setup
- [ ] Keystore-backed secret storage
- [ ] Confirmation and audit trail for tool execution

## Milestone 4 — Security and OSINT
- [ ] Tool manifest review and source/license verification
- [ ] OSINT and defensive network modules
- [ ] Compatibility review for Nmap and Metasploit
- [ ] Lab-only workflows, scope confirmation and audit logging
- [ ] Honest labels for root/kernel/USB/Wi-Fi monitor-mode requirements

## Milestone 5 — Release engineering
- [ ] Signed release builds, SBOM and third-party notices
- [ ] API 24 through current Android test matrix
- [ ] Accessibility, localization, crash and performance testing
