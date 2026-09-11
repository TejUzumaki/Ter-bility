# Old Ter-bility

> **DISCONTINUED**

This project has been discontinued and is no longer under active development.

Ter-bility was an experimental project created to explore the idea of building a real terminal environment for Android, including terminal interaction, process execution, Linux-style tooling, and eventually a Linux desktop distribution.

The current implementation was ultimately determined to be the wrong foundation for the project's long-term goals, so development on this repository has ended.

The developer is now working on a **new project based on the same core idea**, with a cleaner architecture and a stronger focus on understanding and implementing the underlying technology correctly rather than extending the previous implementation.

This repository is preserved as an **archived development record**.

No further feature development is planned here.

---

## Status

**Project:** Discontinued  
**Development:** Stopped  
**Repository:** Preserved for historical/reference purposes  
**Successor:** A new terminal project is being developed separately

## Why was it discontinued?

The original implementation grew around a simplified command-execution model rather than being designed from the beginning around the actual architecture of a terminal.

A real terminal requires substantially more than displaying command output. It involves concepts such as:

- terminal emulation
- PTYs
- shells
- processes and signals
- terminal input/output
- ANSI/VT control sequences
- Linux userland
- executable binaries
- filesystem/environment management
- package management
- platform-specific execution
- Android and Linux frontends

The successor project will be designed around these concepts from the beginning.

## License

See the repository history and existing project files for the licensing information applicable to the original code.

---

**This repository is no longer the active Ter-bility project.**
