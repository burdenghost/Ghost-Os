# Security model

- Android's application sandbox is the outer security boundary. A PRoot-style guest is not a virtual machine and does not provide a separate kernel.
- Never fall back to executing arbitrary input through Android's host shell.
- Store the guest filesystem under app-private storage by default.
- Verify downloaded runtime and root filesystem artifacts against pinned cryptographic hashes before installation.
- Reject archive path traversal, escaping symlinks, oversized archives, and insufficient-storage conditions.
- Keep cloud AI opt-in. Store provider credentials with Android Keystore-backed encryption; never place secrets in shell history or logs.
- Require user review before AI-proposed commands with side effects.
- Review security and OSINT modules for license, source integrity, ABI, dependencies, permissions, network behavior, and authorized-use scope.
- Report root, raw sockets, monitor mode, kernel modules, USB OTG, and other restricted capabilities as unavailable when unsupported.
