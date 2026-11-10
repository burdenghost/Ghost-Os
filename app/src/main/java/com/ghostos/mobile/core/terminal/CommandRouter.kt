package com.ghostos.mobile.core.terminal

/** Safe bootstrap router. It does not execute Android host-shell commands. */
class CommandRouter {
    fun execute(raw: String): CommandResult {
        val command = raw.trim()
        if (command.isEmpty()) return CommandResult(emptyList())
        val name = command.substringBefore(" ").lowercase()
        val output = when (name) {
            "help" -> listOf("Available built-ins:", "  help       Show this help", "  about      Show build information", "  modules    Show module status", "  clear      Clear terminal", "  whoami     Show app identity", "  diagnostics Show Android/runtime diagnostics", "  logs       Open the in-app diagnostic log", "  clearlogs  Clear the saved diagnostic log", "", "Guest Linux commands are available only after Debian starts.")
            "about" -> listOf("Ghost OS Mobile 0.1.0-alpha", "Native Android host with planned isolated Linux userspace", "Hybrid AI: local tools first, cloud providers optional")
            "modules" -> listOf("[planned] Linux runtime", "[planned] Package manager", "[planned] Desktop", "[planned] AI bridge", "[planned] Security toolkit")
            "whoami" -> listOf("ghostos-app (Android app identity; not a Linux user)")
            "clear" -> listOf("\u0000CLEAR\u0000")
            else -> listOf("Command unavailable in bootstrap mode: " + name, "Install and start a verified Linux runtime before enabling guest command execution.")
        }
        return CommandResult(output.map { TerminalLine(it, if (it.startsWith("Command unavailable")) TerminalLine.Kind.ERROR else TerminalLine.Kind.OUTPUT) })
    }
}
