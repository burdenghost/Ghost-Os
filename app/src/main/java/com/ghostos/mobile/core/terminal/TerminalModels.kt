package com.ghostos.mobile.core.terminal

data class TerminalLine(val text: String, val kind: Kind = Kind.OUTPUT) {
    enum class Kind { COMMAND, OUTPUT, ERROR, SYSTEM }
}
data class CommandResult(val lines: List<TerminalLine>)
