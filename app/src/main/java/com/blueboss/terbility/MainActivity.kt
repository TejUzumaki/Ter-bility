package com.blueboss.terbility

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var terminalOutput: TextView
    private lateinit var commandInput: EditText
    private lateinit var sendButton: Button
    private lateinit var scrollView: ScrollView
    
    // Pass the app's private files directory to our execution engine
    private val terminalExec = TerminalExec(filesDir)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        sendButton = findViewById(R.id.sendButton)
        scrollView = findViewById(R.id.scrollView)

        // Welcome message showing the actual working directory
        printToTerminal("Ter-bility [Version 1.0]\n(c) Blue Boss. All rights reserved.\n")
        printToTerminal("Working directory: ${filesDir.absolutePath}\n\n")

        sendButton.setOnClickListener {
            executeCommand()
        }

        commandInput.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                executeCommand()
                true
            } else {
                false
            }
        }
    }

    private fun executeCommand() {
        val command = commandInput.text.toString().trim()
        if (command.isEmpty()) return

        // Echo the command to the terminal output
        printToTerminal("user@ter-bility:~\$ $command\n")

        // Execute and print the result
        val output = terminalExec.execute(command)
        printToTerminal(output)

        // Clear the input field
        commandInput.text.clear()
    }

    private fun printToTerminal(text: String) {
        terminalOutput.append(text)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
