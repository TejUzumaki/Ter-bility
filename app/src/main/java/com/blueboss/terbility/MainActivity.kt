package com.blueboss.terbility

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var terminalOutput: TextView
    private lateinit var commandInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var promptText: TextView
    private lateinit var terminalExec: TerminalExec
    private lateinit var sessionListLayout: android.widget.LinearLayout
    private lateinit var mainLayout: android.widget.LinearLayout
    private lateinit var extraKeysBar: android.widget.LinearLayout

    private var historyIndex = -1
    private var accentColor = "#FF1493"
    private var isDarkMode = true

    data class Session(var exec: TerminalExec, var output: StringBuilder)
    private val sessions = mutableListOf<Session>()
    private var activeSession = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        scrollView = findViewById(R.id.scrollView)
        promptText = findViewById(R.id.promptText)
        sessionListLayout = findViewById(R.id.session_list)
        mainLayout = findViewById(R.id.main_layout)
        extraKeysBar = findViewById(R.id.extra_keys_bar)

        terminalOutput.setTextIsSelectable(true)

        loadSettings()
        setupSidebar()

        if (sessions.isEmpty()) createNewSession() else switchToSession(0)

        commandInput.requestFocus()

        commandInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                executeCommand()
                true
            } else false
        }

        // Auto-scroll when user types long commands
        commandInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        })

        setupExtraKeys()
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("TerbilityPrefs", Context.MODE_PRIVATE)
        accentColor = prefs.getString("accent_color", "#FF1493") ?: "#FF1493"
        isDarkMode = prefs.getString("theme_mode", "dark") == "dark"

        if (isDarkMode) {
            mainLayout.setBackgroundColor(Color.BLACK)
            terminalOutput.setTextColor(Color.WHITE)
            commandInput.setTextColor(Color.WHITE)
            extraKeysBar.setBackgroundColor(Color.parseColor("#111111"))
            terminalOutput.highlightColor = Color.parseColor("#55FF1493")
        } else {
            mainLayout.setBackgroundColor(Color.WHITE)
            terminalOutput.setTextColor(Color.BLACK)
            commandInput.setTextColor(Color.BLACK)
            extraKeysBar.setBackgroundColor(Color.parseColor("#EEEEEE"))
            terminalOutput.highlightColor = Color.parseColor("#55FF1493")
        }
        promptText.setTextColor(Color.parseColor(accentColor))
    }

    private fun setupSidebar() {
        findViewById<Button>(R.id.btn_new_session).setOnClickListener {
            createNewSession()
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<Button>(R.id.btn_settings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun createNewSession() {
        val newExec = TerminalExec(filesDir)
        val newOutput = StringBuilder()
        
        val asciiArt = "████████╗███████╗██████╗░     ██████╗░██╗██╗░░░░░██╗████████╗██╗░░░██╗<br>" +
                       "╚══██╔══╝██╔════╝██╔══██╗     ██╔══██╗██║██║░░░░░██║╚══██╔══╝╚██╗░██╔╝<br>" +
                       "░░░██║░░░█████╗░░██████╔╝     ██████╔╝██║██║░░░░░██║░░░██║░░░░╚████╔╝░<br>" +
                       "░░░██║░░░██╔══╝░░██╔══██╗     ██╔══██╗██║██║░░░░░██║░░░██║░░░░░╚██╔╝░░<br>" +
                       "░░░██║░░░███████╗██║░░██║     ██████╔╝██║███████╗██║░░░██║░░░░░░██║░░░<br>" +
                       "░░░╚═╝░░░╚══════╝╚═╝░░╚═╝     ╚═════╝░╚═╝╚══════╝╚═╝░░░╚═╝░░░░░░╚═╝░░░<br>"
        
        val textColor = if (isDarkMode) "#FFFFFF" else "#000000"
        newOutput.append("<font color='$accentColor'>$asciiArt</font><br>")
        newOutput.append("<font color='$textColor'>Welcome to Ter-bility a terminal made for challenge by Arpit Falke</font><br><br>")

        sessions.add(Session(newExec, newOutput))
        activeSession = sessions.size - 1
        refreshSessionList()
        renderTerminal()
    }

    private fun switchToSession(index: Int) {
        activeSession = index
        renderTerminal()
    }

    private fun refreshSessionList() {
        sessionListLayout.removeAllViews()
        for (i in sessions.indices) {
            val btn = Button(this)
            btn.text = "Session ${i + 1}"
            btn.setBackgroundColor(0x00000000)
            btn.setTextColor(Color.parseColor("#FFFFFF"))
            btn.setOnClickListener {
                switchToSession(i)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            sessionListLayout.addView(btn)
        }
    }

    private fun renderTerminal() {
        terminalExec = sessions[activeSession].exec
        terminalOutput.text = Html.fromHtml(sessions[activeSession].output.toString(), Html.FROM_HTML_MODE_COMPACT)
        promptText.text = terminalExec.getPromptPath()
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun executeCommand() {
        // Haptic feedback on enter
        commandInput.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        
        val command = commandInput.text.toString()
        historyIndex = -1
        
        val escapedCmd = command.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val prompt = terminalExec.getPromptPath()
        val textColor = if (isDarkMode) "#FFFFFF" else "#000000"

        sessions[activeSession].output.append("<font color='$accentColor'>$prompt</font><font color='$textColor'>$escapedCmd</font><br>")

        val output = terminalExec.execute(command)
        if (output == "___CLEAR___") {
            sessions[activeSession].output.clear()
        } else if (output.isNotEmpty()) {
            val escapedOut = output.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
            sessions[activeSession].output.append("<font color='$textColor'>$escapedOut</font>")
        }

        commandInput.text.clear()
        renderTerminal()
    }

    private fun setupExtraKeys() {
        val buttons = listOf(
            R.id.btn_esc, R.id.btn_tab, R.id.btn_ctrl, R.id.btn_alt, 
            R.id.btn_up, R.id.btn_down, R.id.btn_left, R.id.btn_right, 
            R.id.btn_home, R.id.btn_end, R.id.btn_slash, R.id.btn_pipe
        )
        
        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { 
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                when (id) {
                    R.id.btn_esc -> injectText("")
                    R.id.btn_tab -> injectText("    ")
                    R.id.btn_up -> {
                        if (terminalExec.history.isNotEmpty()) {
                            if (historyIndex == -1) historyIndex = terminalExec.history.size - 1
                            else if (historyIndex > 0) historyIndex--
                            commandInput.setText(terminalExec.history[historyIndex])
                            commandInput.setSelection(commandInput.text.length)
                        }
                    }
                    R.id.btn_down -> {
                        if (historyIndex != -1 && historyIndex < terminalExec.history.size - 1) {
                            historyIndex++
                            commandInput.setText(terminalExec.history[historyIndex])
                            commandInput.setSelection(commandInput.text.length)
                        } else {
                            historyIndex = -1
                            commandInput.text.clear()
                        }
                    }
                    R.id.btn_left -> {
                        val pos = commandInput.selectionStart
                        if (pos > 0) commandInput.setSelection(pos - 1)
                    }
                    R.id.btn_right -> {
                        val pos = commandInput.selectionStart
                        if (pos < commandInput.text.length) commandInput.setSelection(pos + 1)
                    }
                    R.id.btn_home -> commandInput.setSelection(0)
                    R.id.btn_end -> commandInput.setSelection(commandInput.text.length)
                    R.id.btn_slash -> injectText("/")
                    R.id.btn_pipe -> injectText("| ")
                }
            }
        }
    }

    private fun injectText(text: String) {
        val start = commandInput.selectionStart
        val end = commandInput.selectionEnd
        commandInput.text.replace(start, end, text)
        commandInput.setSelection(start + text.length)
    }

    override fun onResume() {
        super.onResume()
        loadSettings() 
        renderTerminal()
    }
}
