package com.blueboss.terbility.terminal.session

import com.blueboss.terbility.terminal.execution.InteractiveShell
import java.io.File

class TerminalSession(private val baseDir: File) {
    val shell: InteractiveShell = InteractiveShell(baseDir)
    val outputBuffer: StringBuilder = StringBuilder()
    var isAlive: Boolean = true
    var currentDir: File = baseDir

    fun start(onOutput: (String) -> Unit) {
        shell.onOutput = onOutput
        shell.start()
    }

    fun executeCommand(command: String) {
        // Handle our visual cd natively to update the prompt correctly
        if (command.trim() == "cd" || command.trim().startsWith("cd ")) {
            val targetArg = if (command.trim() == "cd") "~" else command.trim().removePrefix("cd ").trim()
            val targetPath = when {
                targetArg == "~" -> baseDir.absolutePath
                targetArg.startsWith("/") -> targetArg
                else -> File(currentDir, targetArg).absolutePath
            }
            val newDir = File(targetPath).canonicalFile
            if (newDir.isDirectory) {
                currentDir = newDir
                shell.updateDirectory(newDir)
            } else {
                outputBuffer.append("<font color='#FFFFFF'>cd: no such file or directory: $targetArg<br></font>")
            }
        } else {
            // Send directly to the real shell
            shell.write(command + "\n")
        }
    }

    fun getPromptPath(): String {
        val canonicalBase = baseDir.canonicalFile.absolutePath
        var path = currentDir.canonicalFile.absolutePath
        if (path == canonicalBase) path = "~"
        else if (path.startsWith("$canonicalBase/")) path = "~" + path.removePrefix(canonicalBase)
        return "₹$path "
    }

    fun destroy() {
        shell.destroy()
        isAlive = false
    }
}
