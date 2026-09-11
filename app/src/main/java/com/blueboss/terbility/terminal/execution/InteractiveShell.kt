package com.blueboss.terbility.terminal.execution

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class InteractiveShell(private val baseDir: File) {
    private var process: Process? = null
    private var stdin: OutputStreamWriter? = null
    var isAlive = false
    private var currentDir: File = baseDir

    // Callback for streaming output
    var onOutput: ((String) -> Unit)? = null

    fun start() {
        if (isAlive) return
        try {
            val pb = ProcessBuilder("/system/bin/sh", "-i")
            pb.directory(currentDir)
            pb.redirectErrorStream(true)
            
            val env = pb.environment()
            env["HOME"] = baseDir.absolutePath
            env["PATH"] = "/system/bin:/system/xbin"
            env["TERM"] = "xterm-256color"
            env["PS1"] = "" // Hide default shell prompt, we draw our own
            
            process = pb.start()
            stdin = OutputStreamWriter(process!!.outputStream)
            isAlive = true

            // Background thread to read stdout continuously
            Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        onOutput?.invoke(line + "\n")
                    }
                    isAlive = false
                    onOutput?.invoke("___EXIT___")
                } catch (e: Exception) {
                    Log.e("InteractiveShell", "Read error", e)
                    isAlive = false
                    onOutput?.invoke("___EXIT___")
                }
            }.start()
        } catch (e: Exception) {
            Log.e("InteractiveShell", "Start error", e)
            isAlive = false
        }
    }

    fun write(input: String) {
        try {
            stdin?.write(input)
            stdin?.flush()
        } catch (e: Exception) {
            Log.e("InteractiveShell", "Write error", e)
        }
    }

    fun sendSignal(signal: String) {
        write(signal)
    }

    fun updateDirectory(newDir: File) {
        currentDir = newDir
        process?.let {
            // Update the actual shell process directory
            stdin?.write("cd ${newDir.absolutePath}\n")
            stdin?.flush()
        }
    }

    fun destroy() {
        process?.destroy()
        isAlive = false
    }
}
