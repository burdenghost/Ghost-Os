# Ghost OS architecture

Ghost OS is a native Android mobile workstation with an independently managed Linux userspace. It is not a Termux wrapper.

## Layers
1. Android host: lifecycle, permissions, storage, UI, networking, notifications.
2. Ghost services: runtime manager, PTY sessions, file bridge, package manager.
3. Linux guest: Debian stable first, rootless userspace execution.
4. Tool modules: development, OSINT, network diagnostics, web, and forensics.

## Runtime constraints
The initial target is a rootless ARM64 Debian environment. The runtime engine must be selected, version-pinned, license-reviewed, and tested on real devices before bundling. PRoot is syscall/path virtualization, not a VM or hardened security boundary. Android's app sandbox remains the outer boundary.

## Terminal
The current router is a safe bootstrap shell. A real terminal requires a PTY connected to the Linux guest, with input/output streaming, resize, signals, cancellation, exit status, and session lifecycle. Never execute arbitrary commands through /system/bin/sh as a fallback.

## Package management
Use signed repository metadata and package hashes, staged installs, dependency resolution, and rollback. Do not run arbitrary remote installer scripts.

## Deferred features
AI integration is out of scope for the current development phase. The workstation will focus on the Debian runtime, terminal, package management, and core tools first.

## Modules
Each module declares upstream, license, pinned source integrity, supported ABIs, dependencies, capabilities, network behavior, maintenance state, and authorized-use category.
