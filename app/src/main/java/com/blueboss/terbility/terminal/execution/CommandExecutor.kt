package com.blueboss.terbility.terminal.execution

import android.os.Build
import android.os.StatFs
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommandExecutor(private val baseDir: File) {
    var currentDir: File = baseDir
    val history = mutableListOf<String>()

    fun execute(command: String): String {
        var trimmedCmd = command.trim()
        if (trimmedCmd.isEmpty()) return ""
        if (trimmedCmd == "cd..") trimmedCmd = "cd .."
        
        history.add(trimmedCmd)
        val parts = trimmedCmd.split(" ", limit = 2)
        val cmdName = parts[0]
        val args = if (parts.size > 1) parts[1] else ""

        return when (cmdName) {
            "cd" -> handleCd(args)
            "pwd" -> currentDir.absolutePath + "\n"
            "ls" -> handleLs(args)
            "mkdir" -> handleMkdir(args)
            "rm" -> handleRm(args)
            "touch" -> handleTouch(args)
            "cat" -> handleCat(args)
            "mv" -> handleMv(args)
            "cp" -> handleCp(args)
            "echo" -> args + "\n"
            "clear" -> "___CLEAR___"
            "ter-help" -> getHelpMenu()
            "help" -> getHelpMenu()
            "tree" -> handleTree(currentDir, "")
            "whoami" -> "ter-bility-user\n"
            "date" -> SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault()).format(Date()) + "\n"
            "history" -> getHistory()
            "df" -> getDiskInfo()
            else -> try {
                val pb = ProcessBuilder("/system/bin/sh", "-c", trimmedCmd)
                pb.directory(currentDir)
                pb.redirectErrorStream(true)
                val p = pb.start()
                p.inputStream.bufferedReader().readText() + "\n"
            } catch (e: Exception) {
                "${cmdName}: not found or permission denied\n"
            }
        }
    }

    private fun getHelpMenu(): String {
        return """
┌──────────────────────────────────────────────────────────────┐
│ TER-BILITY NATIVE COMMANDS                                   │
├──────────────────────────────────────────────────────────────┤
│ FILE & DIRECTORY                                             │
│   ls [dir]      - List directory contents                    │
│   cd [dir]      - Change directory (supports .. and ~)       │
│   pwd           - Print working directory                    │
│   mkdir [dir]   - Create directory                           │
│   rm [file]     - Remove file/directory                      │
│   touch [file]  - Create empty file                          │
│   cat [file]    - Print file contents                        │
│   mv [s] [d]    - Move/rename file                           │
│   cp [s] [d]    - Copy file                                  │
│   tree          - Display directory tree structure           │
├──────────────────────────────────────────────────────────────┤
│ SYSTEM & TERMINAL                                            │
│   ter-help      - Show this help menu                        │
│   clear         - Clear the terminal screen                  │
│   history       - Show command history                       │
│   whoami        - Print current user                         │
│   date          - Print current system date/time             │
│   df            - Show disk space information                │
└──────────────────────────────────────────────────────────────┘
""".trimIndent() + "\n"
    }

    private fun getHistory(): String {
        val sb = StringBuilder()
        history.forEachIndexed { index, cmd -> sb.append("${index + 1}  $cmd\n") }
        return sb.toString()
    }

    private fun getDiskInfo(): String {
        val stat = StatFs(baseDir.absolutePath)
        val total = stat.totalBytes / (1024 * 1024)
        val avail = stat.availableBytes / (1024 * 1024)
        return "Filesystem            1M-blocks   Used Available Use% Mounted on\n" +
               "/data/data/com.blueboss.terbility  $total       ${total - avail}       $avail   ${((total-avail)*100/total)}%   /data\n"
    }

    private fun handleCd(args: String): String {
        val targetArg = if (args.isEmpty()) "~" else args.trim()
        val targetPath = when {
            targetArg == "~" -> baseDir.absolutePath
            targetArg.startsWith("/") -> targetArg
            else -> File(currentDir, targetArg).absolutePath
        }
        return try {
            val newDir = File(targetPath).canonicalFile
            if (newDir.isDirectory) { currentDir = newDir; "" }
            else "cd: no such file or directory: $targetArg\n"
        } catch (e: Exception) { "cd: error: ${e.message}\n" }
    }

    private fun handleLs(args: String): String {
        val targetDir = if (args.isEmpty()) currentDir else File(currentDir, args)
        if (!targetDir.isDirectory) return "ls: cannot access '$args'\n"
        return targetDir.listFiles()?.joinToString("\n") { it.name } + "\n"
    }

    private fun handleMkdir(args: String): String {
        if (args.isEmpty()) return "mkdir: missing operand\n"
        return if (File(currentDir, args).mkdirs()) "" else "mkdir: cannot create directory '$args'\n"
    }

    private fun handleRm(args: String): String {
        if (args.isEmpty()) return "rm: missing operand\n"
        val file = File(currentDir, args)
        return if (file.deleteRecursively()) "" else "rm: cannot remove '$args'\n"
    }

    private fun handleTouch(args: String): String {
        if (args.isEmpty()) return "touch: missing operand\n"
        File(currentDir, args).createNewFile()
        return ""
    }

    private fun handleCat(args: String): String {
        if (args.isEmpty()) return "cat: missing operand\n"
        val file = File(currentDir, args)
        return if (file.exists()) file.readText() + "\n" else "cat: $args: no such file or directory\n"
    }

    private fun handleMv(args: String): String {
        val parts = args.split(" ", limit = 2)
        if (parts.size < 2) return "mv: missing destination\n"
        val src = File(currentDir, parts[0])
        val dst = File(currentDir, parts[1])
        return if (src.renameTo(dst)) "" else "mv: failed to move\n"
    }

    private fun handleCp(args: String): String {
        val parts = args.split(" ", limit = 2)
        if (parts.size < 2) return "cp: missing destination\n"
        val src = File(currentDir, parts[0])
        val dst = File(currentDir, parts[1])
        return try { src.copyTo(dst, true); "" } catch (e: Exception) { "cp: failed\n" }
    }

    private fun handleTree(dir: File, prefix: String): String {
        val sb = StringBuilder()
        val files = dir.listFiles()?.sortedBy { it.name } ?: return ""
        files.forEachIndexed { index, file ->
            val isLast = index == files.size - 1
            sb.append(prefix).append(if (isLast) "└── " else "├── ").append(file.name).append("\n")
            if (file.isDirectory) {
                val newPrefix = prefix + if (isLast) "    " else "│   "
                sb.append(handleTree(file, newPrefix))
            }
        }
        return sb.toString()
    }

    fun getPromptPath(): String {
        val canonicalBase = baseDir.canonicalFile.absolutePath
        var path = currentDir.canonicalFile.absolutePath
        if (path == canonicalBase) path = "~"
        else if (path.startsWith("$canonicalBase/")) path = "~" + path.removePrefix(canonicalBase)
        return "₹$path "
    }
}
