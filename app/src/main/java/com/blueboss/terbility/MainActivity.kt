package com.blueboss.terbility

import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var terminalOutput: TextView
    private lateinit var commandInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var terminalExec: TerminalExec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        terminalExec = TerminalExec(filesDir)

        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        scrollView = findViewById(R.id.scrollView)

        printToTerminal("Ter-bility [Version 1.0]\n(c) Blue Boss. All rights reserved.\n\n")
        updatePrompt()

        // Listen for Enter key to execute command
        commandInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                executeCommand()
                true
            } else {
                false
            }
        }
    }

    private fun executeCommand() {
        val command = commandInput.text.toString()
        
        // Escape HTML to prevent parsing errors with symbols like < or >
        val escapedCmd = command.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val prompt = terminalExec.getPromptPath()

        // Print the user's command in white, prompt in pink
        printToTerminal("<font color='#FF1493'>$prompt</font><font color='#FFFFFF'>$escapedCmd</font><br>")

        // Execute and print output
        val output = terminalExec.execute(command)
        if (output.isNotEmpty()) {
            val escapedOut = output.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
            printToTerminal("<font color='#FFFFFF'>$escapedOut</font>")
        }

        commandInput.text.clear()
        updatePrompt()
    }

    private fun updatePrompt() {
        // Place the pink prompt directly next to where the user types
        commandInput.hint = ""
        commandInput.setHint(Html.fromHtml("<font color='#FF1493'>" + terminalExec.getPromptPath() + "</font>", Html.FROM_HTML_MODE_COMPACT))
    }

    private fun printToTerminal(html: String) {
        terminalOutput.append(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
