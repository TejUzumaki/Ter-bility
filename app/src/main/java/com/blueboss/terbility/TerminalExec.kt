package com.blueboss.terbility

import java.io.File

class TerminalExec(private val baseDir: File) {
    var currentDir: File = baseDir
    val history = mutableListOf<String>()

    fun execute(command: String): String {
        var trimmedCmd = command.trim()
        if (trimmedCmd.isEmpty()) return ""
        
        // Auto-correct cd.. to cd ..
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
            "help" -> "Available commands: cd, ls, pwd, mkdir, rm, touch, cat, mv, cp, echo, tree, clear, help\n"
            "tree" -> handleTree(currentDir, "")
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
