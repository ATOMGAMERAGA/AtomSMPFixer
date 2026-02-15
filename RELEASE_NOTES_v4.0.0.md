# Release Notes - AtomSMPFixer v4.0.0

## 🚀 Major Release

This is a major update focusing on code quality, internationalization (i18n), and security hardening.

### ✨ Key Changes

- **Internationalization (i18n):**
  - Refactored `AtomFixCommand` to fully support `messages_en.yml` and `messages_tr.yml`.
  - Removed over 90 hardcoded Turkish strings from the codebase.
  - Added comprehensive localization for all command outputs and error messages.

- **Security & Stability:**
  - Upgraded dependencies and project structure to version 4.0.0.
  - Fixed security vulnerabilities in WebPanel (rate limiting and payload handling).
  - Improved error handling and logging across all modules (replaced stack traces with proper logging).

- **Code Quality:**
  - Removed legacy code and version comments.
  - Standardized versioning across all modules.
  - Fixed banner formatting.

- **Modules:**
  - Updated Velocity module version.
  - improved config versioning to 4.0.0.

### 📦 Installation

1. Stop your server.
2. Replace the old JAR with `AtomSMPFixer-4.0.0.jar`.
3. (Optional) Backup your `config.yml` and `messages_*.yml` files.
4. Start the server. The plugin will automatically update the config version.

### 📝 Notes

- Please check `messages_en.yml` or `messages_tr.yml` for new configuration keys if you wish to customize the new messages.
