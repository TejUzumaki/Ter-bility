package com.blueboss.terbility

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TerminalExec(private val context: Context, private val baseDir: File) {
    var currentDir: File = baseDir
    private val busyboxBin = File(baseDir, "busybox")

    init {
        setupBusybox()
    }

    private fun setupBusybox() {
        if (!busyboxBin.exists()) {
            try {
                // Copy raw binary from assets to private storage
                context.assets.open("busybox").use { input ->
                    busyboxBin.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                // Set executable permission
                busyboxBin.setExecutable(true)
                // Install all command symlinks (ls, tree, vi, etc.) into our baseDir
                val installProcess = ProcessBuilder(busyboxBin.absolutePath, "--install", "-s", baseDir.absolutePath)
                installProcess.directory(baseDir)
                installProcess.start().waitFor()
            } catch (e: Exception) {
                // Ignore if it fails, app will just fallback to system commands
            }
        }
    }

    fun execute(command: String): String {
        val trimmedCmd = command.trim()
        if (trimmedCmd.isEmpty()) return ""

        if (trimmedCmd == "cd" || trimmedCmd.startsWith("cd ")) {
            val targetArg = if (trimmedCmd == "cd") "" else trimmedCmd.removePrefix("cd ").trim()
            
            // Calculate the target path
            val targetPath = when {
                targetArg.isEmpty() || targetArg == "~" -> baseDir.absolutePath
                targetArg.startsWith("/") -> targetArg
                else -> File(currentDir, targetArg).absolutePath
            }
            
            return try {
                // canonicalFile automatically resolves '.' and '..' mathematically
                val newDir = File(targetPath).canonicalFile
                if (newDir.isDirectory) {
                    currentDir = newDir
                    "" // Success: No output
                } else {
                    "cd: no such file or directory: $targetArg\n"
                }
            } catch (e: Exception) {
                "cd: error: ${e.message}\n"
            }
        }

        return try {
            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
            processBuilder.redirectErrorStream(true)
            processBuilder.directory(currentDir)

            val env = processBuilder.environment()
            env["HOME"] = baseDir.absolutePath
            env["PWD"] = currentDir.absolutePath
            // Prepend our Busybox directory to PATH so our commands are found first
            env["PATH"] = baseDir.absolutePath + ":" + (env["PATH"] ?: "/system/bin")

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
