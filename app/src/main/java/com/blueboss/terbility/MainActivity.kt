package com.blueboss.terbility

import android.os.Bundle
import android.text.Html
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
    private lateinit var promptText: TextView
    private lateinit var terminalExec: TerminalExec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        terminalExec = TerminalExec(this, filesDir)

        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        scrollView = findViewById(R.id.scrollView)
        promptText = findViewById(R.id.promptText)

        printToTerminal("Ter-bility [Version 1.0]\n(c) Blue Boss. All rights reserved.\n\n")
        updatePrompt()

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
        
        val escapedCmd = command.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val prompt = terminalExec.getPromptPath()

        printToTerminal("<font color='#FF1493'>$prompt</font><font color='#FFFFFF'>$escapedCmd</font><br>")

        val output = terminalExec.execute(command)
        if (output.isNotEmpty()) {
            val escapedOut = output.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
            printToTerminal("<font color='#FFFFFF'>$escapedOut</font>")
        }

        commandInput.text.clear()
        updatePrompt()
    }

    private fun updatePrompt() {
        // Update the standalone TextView, so it never disappears
        promptText.text = terminalExec.getPromptPath()
    }

    private fun printToTerminal(html: String) {
        terminalOutput.append(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
