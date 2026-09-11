package com.blueboss.terbility.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.blueboss.terbility.R
import com.blueboss.terbility.terminal.session.TerminalSession

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var terminalOutput: TextView
    private lateinit var commandInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var promptText: TextView
    private lateinit var sessionListLayout: android.widget.LinearLayout

    private var historyIndex = -1
    private var accentColor = "#FF1493"
    private val history = mutableListOf<String>()

    private val sessions = mutableListOf<TerminalSession>()
    private var activeSession = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // STRICT DEFAULT DARK MODE
        val prefs = getSharedPreferences("TerbilityPrefs", Context.MODE_PRIVATE)
        if (!prefs.contains("theme_mode")) {
            prefs.edit().putString("theme_mode", "dark").apply()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        terminalOutput = findViewById(R.id.terminalOutput)
        commandInput = findViewById(R.id.commandInput)
        scrollView = findViewById(R.id.scrollView)
        promptText = findViewById(R.id.promptText)
        sessionListLayout = findViewById(R.id.session_list)

        terminalOutput.setTextIsSelectable(true)
        loadAccentColor()
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

        setupExtraKeys()
    }

    private fun loadAccentColor() {
        val prefs = getSharedPreferences("TerbilityPrefs", Context.MODE_PRIVATE)
        accentColor = prefs.getString("accent_color", "#FF1493") ?: "#FF1493"
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
        val newSession = TerminalSession(filesDir)
        
        val asciiArt = "████████╗███████╗██████╗░     ██████╗░██╗██╗░░░░░██╗████████╗██╗░░░██╗<br>" +
                       "╚══██╔══╝██╔════╝██╔══██╗     ██╔══██╗██║██║░░░░░██║╚══██╔══╝╚██╗░██╔╝<br>" +
                       "░░░██║░░░█████╗░░██████╔╝     ██████╔╝██║██║░░░░░██║░░░██║░░░░╚████╔╝░<br>" +
                       "░░░██║░░░██╔══╝░░██╔══██╗     ██╔══██╗██║██║░░░░░██║░░░██║░░░░░╚██╔╝░░<br>" +
                       "░░░██║░░░███████╗██║░░██║     ██████╔╝██║███████╗██║░░░██║░░░░░░██║░░░<br>" +
                       "░░░╚═╝░░░╚══════╝╚═╝░░╚═╝     ╚═════╝░╚═╝╚══════╝╚═╝░░░╚═╝░░░░░░╚═╝░░░<br>"
        
        val textColor = "#FFFFFF" // Dark mode is default
        
        newSession.outputBuffer.append("<font color='$accentColor'>$asciiArt</font><br>")
        newSession.outputBuffer.append("<font color='$textColor'>Welcome to Ter-bility a terminal made for challenge by Arpit Falke</font><br><br>")

        sessions.add(newSession)
        activeSession = sessions.size - 1
        
        // Start the real background shell
        newSession.start { output ->
            runOnUiThread {
                if (output == "___EXIT___") {
                    newSession.outputBuffer.append("<font color='#FF0000'>[Process exited]<br></font>")
                } else {
                    val escapedOut = output.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>")
                    newSession.outputBuffer.append("<font color='#FFFFFF'>$escapedOut</font>")
                }
                if (activeSession == sessions.indexOf(newSession)) renderTerminal()
            }
        }
        
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
            btn.setTextColor(Color.WHITE)
            btn.setOnClickListener {
                switchToSession(i)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            sessionListLayout.addView(btn)
        }
    }

    private fun renderTerminal() {
        val active = sessions[activeSession]
        terminalOutput.text = Html.fromHtml(active.outputBuffer.toString(), Html.FROM_HTML_MODE_COMPACT)
        promptText.text = active.getPromptPath()
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun executeCommand() {
        val command = commandInput.text.toString()
        historyIndex = -1
        
        if (command.isNotEmpty()) {
            history.add(command)
        }
        
        val escapedCmd = command.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
        val prompt = sessions[activeSession].getPromptPath()

        sessions[activeSession].outputBuffer.append("<font color='$accentColor'>$prompt</font><font color='#FFFFFF'>$escapedCmd</font><br>")
        
        // Send to real shell
        sessions[activeSession].executeCommand(command)

        commandInput.text.clear()
        renderTerminal()
        
        commandInput.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(commandInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupExtraKeys() {
        val buttons = listOf(
            R.id.btn_esc, R.id.btn_tab, R.id.btn_ctrl, R.id.btn_alt, 
            R.id.btn_up, R.id.btn_down, R.id.btn_left, R.id.btn_right, 
            R.id.btn_home, R.id.btn_end, R.id.btn_slash, R.id.btn_pipe
        )
        
        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener { 
                when (id) {
                    R.id.btn_esc -> sessions[activeSession].shell.sendSignal("\u001B") // ESC
                    R.id.btn_tab -> sessions[activeSession].shell.sendSignal("\t") // TAB
                    R.id.btn_ctrl -> sessions[activeSession].shell.sendSignal("\u0003") // CTRL+C
                    R.id.btn_alt -> { /* Alt sequences require complex mapping, left empty for now */ }
                    R.id.btn_up -> {
                        if (history.isNotEmpty()) {
                            if (historyIndex == -1) historyIndex = history.size - 1
                            else if (historyIndex > 0) historyIndex--
                            commandInput.setText(history[historyIndex])
                            commandInput.setSelection(commandInput.text.length)
                        }
                    }
                    R.id.btn_down -> {
                        if (historyIndex != -1 && historyIndex < history.size - 1) {
                            historyIndex++
                            commandInput.setText(history[historyIndex])
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
        loadAccentColor() 
        renderTerminal()
        commandInput.requestFocus()
    }
}
