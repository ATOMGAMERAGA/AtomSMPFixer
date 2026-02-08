# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AtomSMPFixer is a Minecraft Paper 1.21.4 exploit fixer plugin written in Java 21. It detects and blocks 21 different exploit types (crashers, duplication glitches, packet exploits) using PacketEvents 2.6.0+ for packet-level interception. All user-facing text is in Turkish.

## Build Commands

```bash
# Build the plugin JAR (output: target/AtomSMPFixer-{version}.jar)
mvn clean package

# Build skipping tests
mvn clean package -DskipTests

# Update version
mvn versions:set -DnewVersion=X.X.X
```

Requires Java 21 JDK and Maven 3.8+. No unit tests exist currently — CI validates successful compilation only.

## Architecture

**Singleton entry point:** `AtomSMPFixer` (`com.atomsmp.fixer.AtomSMPFixer`) — orchestrates lifecycle: `onLoad()` initializes PacketEvents API, `onEnable()` boots managers/listeners/commands, `onDisable()` tears down. Access via `AtomSMPFixer.getInstance()`.

**Manager layer** (4 managers, all accessed via getters on the main plugin class):
- `ConfigManager` — YAML config loading with caching and hot-reload
- `MessageManager` — MiniMessage format message rendering and permission-based sending
- `LogManager` — Async file writing with daily rotation and 7-day retention
- `ModuleManager` — Module registration, lifecycle (enable/disable/toggle), and statistics

**Module system:** All 21 exploit fixers extend `AbstractModule`. Each module has a `name` matching its config key under `moduller.{name}` in `config.yml`. Modules use helper methods (`getConfigBoolean`, `getConfigInt`, etc.) that auto-prefix the config path. New modules must be registered in `AtomSMPFixer.registerModules()`.

**Listener layer** (3 listeners):
- `PacketListener` — PacketEvents API integration for packet filtering (registered with PacketEvents event manager)
- `BukkitListener` — Standard Bukkit block/entity events
- `InventoryListener` — Inventory click/close events

**Utilities:** `PacketUtils`, `BookUtils`, `NBTUtils` for validation; `CooldownManager` for rate limiting; `PerformanceMonitor` for timing.

## Key Conventions

- **Config keys are in Turkish** (e.g., `moduller.cok-fazla-kitap.aktif`, `moduller.paket-exploit.max-paket-boyutu`). The module `name` field must match the Turkish config key exactly.
- **Thread safety:** Uses `ConcurrentHashMap` and `AtomicLong` throughout. Module `enabled` state is `volatile`.
- **PacketEvents is a required dependency** (scope: provided). The plugin disables itself if PacketEvents is not present. PacketEvents is loaded in `onLoad()` and initialized in `onEnable()`.
- **Resource filtering is enabled** in Maven — `${project.version}` in `plugin.yml` is substituted at build time.
- **All dependencies are `provided` scope** — nothing is shaded into the JAR except the project's own classes.

## Package Structure

```
com.atomsmp.fixer
├── AtomSMPFixer.java        # Main plugin singleton
├── command/                  # AtomFixCommand + AtomFixTabCompleter
├── data/                     # PlayerData, ChunkBookTracker
├── listener/                 # PacketListener, BukkitListener, InventoryListener
├── manager/                  # ConfigManager, MessageManager, LogManager, ModuleManager
├── module/                   # AbstractModule + 21 concrete exploit fixer modules
└── util/                     # CooldownManager, PacketUtils, NBTUtils, BookUtils, PerformanceMonitor
```

## Adding a New Module

1. Create a class in `com.atomsmp.fixer.module` extending `AbstractModule`
2. Pass the Turkish config key as the `name` parameter to super constructor
3. Add corresponding config section under `moduller.{name}` in `src/main/resources/config.yml`
4. Register the module in `AtomSMPFixer.registerModules()`
5. Wire the module's check logic into the appropriate listener (`PacketListener`, `BukkitListener`, or `InventoryListener`)
