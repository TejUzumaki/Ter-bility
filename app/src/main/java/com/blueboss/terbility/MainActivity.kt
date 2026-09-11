package com.blueboss.terbility

import android.os.Bundle
import android.text.Html
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
    private lateinit var scrollView: ScrollView
    private lateinit var promptText: TextView
    private lateinit var terminalExec: TerminalExec

    private var historyIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        terminalExec = TerminalExec(filesDir)

        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        scrollView = findViewById(R.id.scrollView)
        promptText = findViewById(R.id.promptText)

        printToTerminal("<font color='#FF1493'>Welcome to Ter-bility a terminal made for challenge by Arpit Falke</font><br><br>")
        updatePrompt()

        commandInput.requestFocus()
        
        commandInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                executeCommand()
                true
            } else {
                false
            }
        }

        setupExtraKeys()
    }

    private fun setupExtraKeys() {
        val escBtn = findViewById<Button>(R.id.btn_esc)
        val tabBtn = findViewById<Button>(R.id.btn_tab)
        val ctrlBtn = findViewById<Button>(R.id.btn_ctrl)
        val altBtn = findViewById<Button>(R.id.btn_alt)
        val upBtn = findViewById<Button>(R.id.btn_up)
        val downBtn = findViewById<Button>(R.id.btn_down)
        val slashBtn = findViewById<Button>(R.id.btn_slash)
        val pipeBtn = findViewById<Button>(R.id.btn_pipe)

        escBtn.setOnClickListener { injectText("") } // ESC requires complex handling, leave empty for now
        tabBtn.setOnClickListener { injectText("    ") } // Simulate tab space
        ctrlBtn.setOnClickListener { } // Placeholder
        altBtn.setOnClickListener { } // Placeholder
        
        upBtn.setOnClickListener {
            if (terminalExec.history.isNotEmpty()) {
                if (historyIndex == -1) historyIndex = terminalExec.history.size - 1
                else if (historyIndex > 0) historyIndex--
                commandInput.setText(terminalExec.history[historyIndex])
                commandInput.setSelection(commandInput.text.length)
            }
        }

        downBtn.setOnClickListener {
            if (historyIndex != -1 && historyIndex < terminalExec.history.size - 1) {
                historyIndex++
                commandInput.setText(terminalExec.history[historyIndex])
                commandInput.setSelection(commandInput.text.length)
            } else {
                historyIndex = -1
                commandInput.text.clear()
            }
        }

        slashBtn.setOnClickListener { injectText("/") }
        pipeBtn.setOnClickListener { injectText("| ") }
    }

    private fun injectText(text: String) {
        val start = commandInput.selectionStart
        val end = commandInput.selectionEnd
        commandInput.text.replace(start, end, text)
        commandInput.setSelection(start + text.length)
    }

    private fun executeCommand() {
        val command = commandInput.text.toString()
        historyIndex = -1
        
        val escapedCmd = command.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val prompt = terminalExec.getPromptPath()

        printToTerminal("<font color='#FF1493'>$prompt</font><font color='#FFFFFF'>$escapedCmd</font><br>")

        val output = terminalExec.execute(command)
        if (output == "___CLEAR___") {
            terminalOutput.text = ""
        } else if (output.isNotEmpty()) {
            val escapedOut = output.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
            printToTerminal("<font color='#FFFFFF'>$escapedOut</font>")
        }

        commandInput.text.clear()
        updatePrompt()
    }

    private fun updatePrompt() {
        promptText.text = terminalExec.getPromptPath()
    }

    private fun printToTerminal(html: String) {
        terminalOutput.append(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
