# tetrix

Tetrix is a Kotlin/libGDX implementation of a block-stacking puzzle with an additional rotor mechanic. The project uses KTX utilities and targets the desktop platform via LWJGL3.

## Platforms

- `core`: Main module with game logic and shared code.
- `lwjgl3`: Desktop launcher using LWJGL3.

## Prerequisites

- Java JDK 8 or newer (project targets JVM 1.8)
- No need to install Gradle: the Gradle Wrapper (`gradlew`/`gradlew.bat`) is included

## Run (Desktop)

From the project root:
- Windows: `gradlew.bat lwjgl3:run`
- macOS/Linux: `./gradlew lwjgl3:run`

This starts the desktop application with the LWJGL3 backend.

## Build

Build a runnable JAR:
1. Run `gradlew.bat lwjgl3:jar` (Windows) or `./gradlew lwjgl3:jar` (macOS/Linux)
2. Find the artifact at `lwjgl3/build/libs/tetrix-<version>.jar`

Tip: Use `--refresh-dependencies` if you suspect dependency cache issues.

## Controls (default)

- Move Up: W
- Move Down: S
- Move Left: A
- Move Right: D
- Snap/Hard Drop: Space
- Rotate Tetromino Left: Q
- Rotate Tetromino Right: E
- Rotate Rotor Left: J
- Rotate Rotor Right: L
- Pause/Resume: ESC

These defaults come from `core/src/main/kotlin/ch/tetrix/shared/models/PlayerConfig.kt` and can be changed in code or through option menus if provided.

## Project Structure

- `core/` — game logic, screens, stages, services, and shared models
- `lwjgl3/` — desktop launcher and platform-specific configuration
- `assets/` — textures, sounds, fonts, UI skin and other resources
- `docs/` — documentation (e.g., UML diagrams)

## Gradle tips

- `build` — builds sources and archives of every subproject
- `clean` — removes build folders
- `lwjgl3:run` — runs the desktop app
- `lwjgl3:jar` — builds the desktop JAR in `lwjgl3/build/libs`
- `test` — runs unit tests (if any)
- Useful flags: `--daemon`, `--offline`, `--continue`, `--refresh-dependencies`

Most non-subproject-specific tasks can be run with the `<module>:` prefix (e.g., `core:clean`).

## License

If a license file is present in the repository, the project is licensed under its terms. Otherwise, all rights reserved by the authors.
