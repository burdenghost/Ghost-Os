# Ghost OS

**Ghost OS** is an independent Android mobile Linux workstation. It is designed as a native Android application with an isolated Linux userspace—not as a Termux wrapper.

> Current stage: **0.1.0-alpha**. The Android shell UI has been built and installed on a Samsung device. The current bootstrap terminal supports built-in demo commands only; no Linux userspace or external command execution is connected yet.

## Project goals
- Native Android host application (minimum Android 7 / API 24 target baseline).
- Isolated Debian userspace stored inside the app's private storage.
- Real interactive terminal backed by a PTY and a Linux userspace runtime.
- Linux rootfs installer with download verification, extraction, initialization, and recovery.
- Package management inside the Debian environment.
- Hybrid AI assistant: optional local/offline capabilities and opt-in cloud providers.
- Modular OSINT and defensive security tools, with explicit authorization and safety controls.
- Optional graphical Linux desktop/workstation interface.

## Architecture principles
1. **Independent of Termux:** Ghost OS must manage its own runtime, root filesystem, terminal sessions, and package environment.
2. **No root requirement by default:** Use Android app sandboxing and a userspace runtime. Clearly document features that Android restricts without root or special hardware.
3. **Least privilege:** Keep Linux files in app-private storage; request Android permissions only when a feature needs them.
4. **Verified components:** Pin runtime and rootfs sources, verify checksums/signatures where available, and avoid executing unverified downloads.
5. **Modular design:** Keep Android UI, runtime lifecycle, terminal/PTY, package management, AI, and security modules separated.
6. **Transparent capability states:** UI status labels must reflect actual installed/available functionality, not planned features.

## Roadmap
### Phase 0 — Alpha baseline
- [x] Native Android app builds successfully.
- [x] Bootstrap terminal UI launches on-device.
- [x] Kotlin / Compose compiler compatibility fixed (Kotlin 1.7.20 with Compose Compiler 1.3.2).
- [ ] Preserve the complete source tree and reproducible build instructions in this repository.

### Phase 1 — Linux runtime foundation
- [ ] Select and document a maintained ARM64-compatible userspace runtime strategy.
- [ ] Add runtime lifecycle and app-private filesystem layout.
- [ ] Add verified Debian rootfs download and extraction.
- [ ] Add first-boot initialization and recovery handling.
- [ ] Expose truthful Linux installation state in the UI.

### Phase 2 — Interactive terminal
- [ ] Implement PTY-backed interactive sessions.
- [ ] Connect terminal input/output, resize, signals, and session lifecycle.
- [ ] Validate core commands: pwd, ls, cd, env, uname, python3.
- [ ] Persist shell configuration and session settings safely.

### Phase 3 — Package management
- [ ] Configure Debian repositories and package trust.
- [ ] Implement package install/update/remove workflows.
- [ ] Add storage, network, and failure handling.

### Phase 4 — AI and workstation modules
- [ ] Add provider-neutral AI interface and local/cloud modes.
- [ ] Add permission-gated tool execution and audit trail.
- [ ] Add modular OSINT and defensive security capabilities for authorized use.
- [ ] Evaluate graphical desktop options compatible with Android constraints.

## Current alpha build notes
- Android build task: :app:assembleDebug
- Kotlin Gradle plugin: 1.7.20
- Compose Compiler: 1.3.2
- JDK used for the successful local build: 17.0.16
- AAPT2 in the AndroidIDE/Android Code Studio environment required an explicit local override during the successful build.
- Debug APK generated at app/build/outputs/apk/debug/app-debug.apk (approximately 13 MB).

These are the reported local build conditions; reproduce and verify them from the source tree before treating them as release requirements.

## Repository status
This repository currently contains the project charter and implementation roadmap. The complete Android application source has not yet been imported. The next engineering step is to add the existing working 0.1.0-alpha source tree, then implement the Linux runtime against the actual app architecture.

## Security
Ghost OS is intended for legitimate development, administration, research, and authorized security testing. Network and security features should be designed with explicit user consent, clear scope, and safe defaults.
