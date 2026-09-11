package com.blueboss.terbility

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TerminalExec(private val context: Context, private val baseDir: File) {
    var currentDir: File = baseDir
    private val busyboxBin = File(baseDir, "busybox")

    // List of commands BusyBox will handle for us
    private val applets = listOf("tree", "wget", "vi", "grep", "awk", "sed", "find", "du", "df", "ps", "top", "head", "tail", "less", "tar")

    init {
        setupBusybox()
    }

    private fun setupBusybox() {
        if (!busyboxBin.exists()) {
            try {
                context.assets.open("busybox").use { input ->
                    busyboxBin.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                busyboxBin.setExecutable(true)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun execute(command: String): String {
        val trimmedCmd = command.trim()
        if (trimmedCmd.isEmpty()) return ""

        // Handle 'cd' manually
        if (trimmedCmd == "cd" || trimmedCmd.startsWith("cd ")) {
            val targetArg = if (trimmedCmd == "cd") "" else trimmedCmd.removePrefix("cd ").trim()
            val targetPath = when {
                targetArg.isEmpty() || targetArg == "~" -> baseDir.absolutePath
                targetArg.startsWith("/") -> targetArg
                else -> File(currentDir, targetArg).absolutePath
            }
            return try {
                val newDir = File(targetPath).canonicalFile
                if (newDir.isDirectory) {
                    currentDir = newDir
                    "" 
                } else {
                    "cd: no such file or directory: $targetArg\n"
                }
            } catch (e: Exception) {
                "cd: error: ${e.message}\n"
            }
        }

        return try {
            val parts = trimmedCmd.split(" ", limit = 2)
            val cmdName = parts[0]
            val cmdArgs = if (parts.size > 1) parts[1] else ""

            // Intercept Busybox applets
            val finalCommand = if (applets.contains(cmdName) && busyboxBin.exists()) {
                "$busyboxBin $cmdName $cmdArgs"
            } else {
                trimmedCmd
            }

            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", finalCommand)
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

    fun getPromptPath(): String {
        // Canonicalize BOTH paths so /data/data matches /data/user/0
        val canonicalBase = baseDir.canonicalFile.absolutePath
        var path = currentDir.canonicalFile.absolutePath
        
        if (path == canonicalBase) {
            path = "~"
        } else if (path.startsWith("$canonicalBase/")) {
            path = "~" + path.removePrefix(canonicalBase)
        }
        return "user@ter-bility:$path₹ "
    }
}
