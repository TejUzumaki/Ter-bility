# Ter-bility

A professional, feature-rich terminal emulator for Android, built entirely from Termux using GitHub Actions for CI/CD.

![Version](https://img.shields.io/badge/version-2.0.0-pink)
![Platform](https://img.shields.io/badge/platform-Android-black)
![Language](https://img.shields.io/badge/language-Kotlin-purple)

## Features

### Core Terminal
- **Native Linux Commands:** Built-in execution for `ls`, `cd`, `pwd`, `mkdir`, `rm`, `touch`, `cat`, `mv`, `cp`, `echo`, `tree`, `clear`, and `help`.
- **System Shell Fallback:** Automatically falls back to Android's `/system/bin/sh` for unsupported commands.
- **Path Resolution:** Flawless mathematical resolution of `.`, `..`, and `~` paths.
- **Command History:** Cycle through previously executed commands using the Up/Down keys.

### User Interface
- **True Inline Typing:** Type directly into the terminal screen, just like Termux.
- **Session Manager:** Swipe from the left edge to open the drawer and create/manage multiple terminal sessions.
- **Custom Theming:** Light and Dark mode support.
- **Accent Colors:** Choose between Deep Pink, Retro Green, Cyber Cyan, Alert Orange, or Royal Purple for your prompt.
- **Custom ASCII Art:** Beautiful block-text startup banner.
- **Text Selection:** Long-press to highlight and copy text with a custom pink selection color.

### Extra Keys Bar
- A horizontally scrollable toolbar at the bottom featuring `ESC`, `TAB`, `CTRL`, `ALT`, History Up/Down, Arrow Keys, `HOME`, `END`, `/`, and `|`.
- Haptic feedback on key presses.

## Developer
- **Blue Boss (Arpit Falke)**

## Build
To build this project, simply push to the `main` branch. GitHub Actions will automatically compile the APK and publish it to the Releases page.
