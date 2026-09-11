package com.blueboss.terbility

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class TerminalExec(private val workingDir: File) {
    
    fun execute(command: String): String {
        return try {
            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
            processBuilder.redirectErrorStream(true)
            
            // Force the shell to start inside Ter-bility's private app directory
            processBuilder.directory(workingDir)
            
            // Set HOME andPWD environment variables so commands know where they are
            val env = processBuilder.environment()
            env["HOME"] = workingDir.absolutePath
            env["PWD"] = workingDir.absolutePath
            
            val process = processBuilder.start()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString()
        } catch (e: IOException) {
            "Error executing command: ${e.message}\n"
        } catch (e: InterruptedException) {
            "Execution interrupted: ${e.message}\n"
        }
    }
}
