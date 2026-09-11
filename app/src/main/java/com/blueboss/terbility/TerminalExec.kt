package com.blueboss.terbility

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class TerminalExec {
    // Uses the Android system shell to execute commands
    fun execute(command: String): String {
        return try {
            val processBuilder = ProcessBuilder("/system/bin/sh", "-c", command)
            processBuilder.redirectErrorStream(true)
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
