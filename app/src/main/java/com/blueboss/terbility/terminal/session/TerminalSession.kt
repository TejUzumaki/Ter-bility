package com.blueboss.terbility.terminal.session

import com.blueboss.terbility.terminal.execution.CommandExecutor
import java.io.File

class TerminalSession(private val baseDir: File) {
    val executor: CommandExecutor = CommandExecutor(baseDir)
    val outputBuffer: StringBuilder = StringBuilder()
    var isAlive: Boolean = true

    fun executeCommand(command: String): String {
        return executor.execute(command)
    }

    fun getPromptPath(): String = executor.getPromptPath()
}
