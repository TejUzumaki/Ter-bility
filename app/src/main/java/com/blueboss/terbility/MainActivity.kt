package com.blueboss.terbility

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
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
    private lateinit var terminalExec: TerminalExec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize context-dependent fields here to prevent crash
        terminalExec = TerminalExec(filesDir)

        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        sendButton = findViewById(R.id.sendButton)
        scrollView = findViewById(R.id.scrollView)

        printToTerminal("Ter-bility [Version 1.0]\n(c) Blue Boss. All rights reserved.\n")
        printToTerminal("Working directory: ${filesDir.absolutePath}\n\n")

        sendButton.setOnClickListener {
            executeCommand()
        }

        commandInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND || 
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
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

        printToTerminal("user@ter-bility:~\$ $command\n")
        val output = terminalExec.execute(command)
        printToTerminal(output)
        commandInput.text.clear()
    }

    private fun printToTerminal(text: String) {
        terminalOutput.append(text)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
