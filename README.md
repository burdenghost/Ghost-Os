# Ghost OS — Mobile Workstation

**Status:** 0.1.0-alpha / native Android bootstrap  
**Minimum Android:** 7.0 (API 24)  
**Architecture:** Native Android host + planned isolated Linux userspace + modular tools + hybrid AI

Ghost OS is an independent mobile Linux workstation. It is not based on Termux and does not require Termux. This repository now contains the imported Android alpha source tree, safe built-in terminal prototype, Linux runtime contract, architecture notes, and tool catalog.

> The current alpha does not yet include a working Debian rootfs, PRoot runtime, PTY-backed Linux terminal, package manager, AI provider integrations, or security-tool execution. The bootstrap terminal only supports its documented built-ins. The UI must not claim these capabilities are ready until implementation and device testing are complete.

## Build

- JDK 17
- Android SDK Platform 33
- Android Gradle Plugin 7.3.1
- Gradle 7.4.2
- Kotlin Gradle plugin 1.7.20
- Compose Compiler 1.3.2

Open the directory containing `settings.gradle.kts` as the project root. Build `:app:assembleDebug`. The project owner verified a successful local build using AndroidIDE/Android Code Studio and a local AAPT2 override. A Gradle wrapper is not currently committed.

## Project structure

- `app/` — native Android host and Compose interface
- `app/src/main/java/.../core/terminal/` — safe command router prototype
- `app/src/main/java/.../core/runtime/` — Linux runtime contract; engine integration pending
- `docs/` — architecture, roadmap, and security model
- `tooling/catalog/` — reviewed tool manifest schema

## Roadmap

See [docs/ROADMAP.md](docs/ROADMAP.md). Runtime engine selection and license review are the next major engineering gate. A rootless userspace runtime is not a virtual machine or hardened security boundary; Android's application sandbox remains the outer boundary.

## Security and licensing

Only install pinned, verified runtime and rootfs artifacts. Never execute arbitrary commands on Android's host shell as a fallback. Cloud AI is opt-in and secrets must be stored securely. Security modules are for systems the operator owns or is explicitly authorized to assess. Review upstream licenses before redistributing third-party binaries.

