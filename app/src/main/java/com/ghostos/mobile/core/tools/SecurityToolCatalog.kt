package com.ghostos.mobile.core.tools

data class SecurityTool(
    val id: String,
    val name: String,
    val description: String,
    val packages: List<String> = emptyList(),
    val installable: Boolean = packages.isNotEmpty(),
)

object SecurityToolCatalog {
    val all = listOf(
        SecurityTool(
            id = "nmap",
            name = "Nmap",
            description = "Network discovery and service inventory. Use TCP connect scans (-sT); raw-packet scan modes need capabilities unavailable to ordinary Android apps.",
            packages = listOf("nmap"),
        ),
        SecurityTool(
            id = "sqlmap",
            name = "sqlmap",
            description = "Automated SQL injection testing for applications you own or are explicitly authorized to assess.",
            packages = listOf("sqlmap"),
        ),
        SecurityTool(
            id = "nikto",
            name = "Nikto",
            description = "Web-server configuration and known-file checks for authorized assessments.",
            packages = listOf("nikto"),
        ),
        SecurityTool(
            id = "whois-dns",
            name = "WHOIS + DNS utilities",
            description = "Domain registration lookups and DNS record diagnostics.",
            packages = listOf("whois", "dnsutils"),
        ),
        SecurityTool(
            id = "cli-essentials",
            name = "CLI essentials",
            description = "Git, curl, jq, ripgrep and Python virtual environments for portable security workflows.",
            packages = listOf("git", "curl", "jq", "ripgrep", "python3-venv"),
        ),
        SecurityTool(
            id = "osrframework",
            name = "OSRFramework",
            description = "OSINT framework requested for Ghost OS. Held for dependency, Python-version and ARM64 compatibility validation before offering installation.",
        ),
        SecurityTool(
            id = "metasploit",
            name = "Metasploit Framework",
            description = "Held for ARM64 packaging, storage footprint, dependency and license review. Not advertised as installable until verified.",
        ),
        SecurityTool(
            id = "wireshark",
            name = "Wireshark / tshark",
            description = "Packet-analysis support requires a separate capability review; Android app sandboxing does not grant raw packet capture or monitor mode.",
        ),
    )
}
