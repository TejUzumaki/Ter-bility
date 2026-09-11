package com.blueboss.terbility

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TerminalExec(private val baseDir: File) {
    var currentDir: File = baseDir

    fun execute(command: String): String {
        val trimmedCmd = command.trim()
        if (trimmedCmd.isEmpty()) return ""

        // Manually handle 'cd' because each ProcessBuilder is stateless
        if (trimmedCmd == "cd" || trimmedCmd.startsWith("cd ")) {
            val targetArg = if (trimmedCmd == "cd") "" else trimmedCmd.removePrefix("cd ").trim()
            val newDir = when {
                targetArg.isEmpty() || targetArg == "~" -> baseDir
                targetArg.startsWith("/") -> File(targetArg)
                else -> File(currentDir, targetArg)
            }
            
            return if (newDir.isDirectory) {
                currentDir = newDir
                "" // Success: No output
            } else {
                "cd: no such file or directory: $targetArg\n"
            }
        }

        return try {
            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
            processBuilder.redirectErrorStream(true)
            processBuilder.directory(currentDir)

            val env = processBuilder.environment()
            env["HOME"] = baseDir.absolutePath
            env["PWD"] = currentDir.absolutePath

            val process = processBuilder.start()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString()
        } catch (e: Exception) {
            "Error: ${e.message}\n"
        }
    }

    // Generate the prompt string with Rupee sign
    fun getPromptPath(): String {
        val basePath = baseDir.absolutePath
        var path = currentDir.absolutePath
        if (path == basePath) {
            path = "~"
        } else if (path.startsWith("$basePath/")) {
            path = "~" + path.removePrefix(basePath)
        }
        return "user@ter-bility:$path₹ "
    }
}
