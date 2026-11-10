# Rootless security tools

Ghost OS runs security utilities only inside the app-private Debian guest through the PRoot PTY session. The installer never invokes Android's host shell, requests Android root, or changes device-wide permissions.

## First supported ARM64 package set

The Tools Center installs these packages from the configured Debian repositories:

- Nmap (`nmap`): use TCP connect scans such as `-sT`; raw packet modes are not promised.
- sqlmap (`sqlmap`): authorized application testing only.
- Nikto (`nikto`): authorized web-server checks.
- WHOIS and DNS utilities (`whois`, `dnsutils`).
- CLI essentials (`git`, `curl`, `jq`, `ripgrep`, `python3-venv`).

Install actions start or reuse the Debian PTY session and send an explicit `apt-get` command to that guest shell. Package installation requires a working Debian installation, network access, and sufficient app-private storage. It does not require Android root.

## Held for compatibility review

- OSRFramework: verify maintenance state, Python dependencies and ARM64 compatibility.
- Metasploit Framework: verify ARM64 packaging, dependency/storage footprint and redistribution notices.
- Wireshark/tshark: packet capture depends on capabilities that an ordinary Android app sandbox may not provide.

These entries are deliberately not installable until the checks pass. Do not imply that PRoot supplies a separate kernel, raw sockets, Wi-Fi monitor mode, kernel modules, USB privileges or VM-grade isolation.

## Validation gate

The Android build must pass before shipping. On a real ARM64 device, install Debian, install each supported package, run its version/help command, close and reopen Ghost OS, and confirm the package remains available. Verify that Nmap TCP connect scanning works against an owned lab target. Record failures in the in-app diagnostics. A successful APK build alone does not establish device-level tool functionality.
