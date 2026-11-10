# Ghost OS roadmap

## Milestone 0 — Modern Android bootstrap
- [x] Native Android project skeleton and Compose dashboard
- [x] Safe built-in terminal prototype
- [x] Drop legacy Android support; set minimum Android 9 / API 28
- [x] Successful on-device debug build reported by project owner
- [x] Upgrade to AGP 9.5.0 / Gradle 9.6.0 / Kotlin 2.4.10 / JDK 17
- [x] Import alpha source tree into this repository
- [x] Pin Gradle 9.6.0 wrapper distribution
- [ ] Add wrapper scripts/JAR and CI build

## Milestone 1 — Linux runtime foundation
- [ ] Pin and integrate maintained ARM64 PRoot engine as a source dependency
- [ ] Complete GPL/MIT license inventory and third-party notices
- [ ] Add verified Debian rootfs manifest and downloader
- [ ] Safe extraction, disk checks, install progress, cancellation, recovery
- [ ] Runtime lifecycle and status persistence
- [ ] Android 9+ device matrix, target-SDK 35 runtime tests, and 16 KB page-size validation

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
