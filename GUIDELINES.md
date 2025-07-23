# Coding Guidelines for LibGDX/KTX Application

Maintaining clean, readable, and maintainable code is essential for any software project. These guidelines are tailored for our LibGDX/KTX game to ensure consistency, performance, and scalability throughout development.

---

## 1. Project Structure

* **Modular Layout**: Organize code into clear modules/packages such as `core`, `screens`, `ui`, `assets`, `physics`.
* **Single Responsibility**: Each class/file should have one clear purpose. Screens manage game states, Actors handle UI, and systems (e.g., physics) reside in dedicated packages.
* **Resource Organization**: Store assets in a consistent directory hierarchy (`assets/images`, `assets/sounds`, `assets/skins`, `assets/maps`). Mirror this in code using the KTX `asset {}` DSL for clarity.

## 2. Coding Standards

* **Kotlin Style**: Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). Use `val` over `var` whenever possible.
* **KTX Extensions**: Prefer KTX extension functions (`ktx.app`, `ktx.assets`, `ktx.async`) for concise, idiomatic Kotlin code.
* **Immutability**: Favor immutable data structures (`kotlin.collections` or `ktx.collections`) to reduce side-effects and simplify debugging.
* **Null Safety**: Embrace Kotlin’s null-safety features. Avoid using the non-null asserted call (`!!`) except in truly unavoidable scenarios.

## 3. Naming Conventions

* **Classes & Interfaces**: UpperCamelCase (e.g., `MainMenuScreen`, `PlayerController`).
* **Functions & Variables**: lowerCamelCase (e.g., `loadAssets()`, `playerHealth`).
* **Constants**: Use `UPPER_SNAKE_CASE` for compile-time constants (`const val`).
* **Package Names**: all lowercase, dot-separated (e.g., `com.mycompany.mygame.ui`).

## 4. Dependency Injection (KTX-Inject)

* **Binding**: Bind singletons and factories in the entry point (e.g., `MainGame.kt`) using `ktx.inject`:

  ```kotlin
  val context = Context()
  context.bindSingleton(AssetManager())
  context.bindSingleton(SpriteBatch())
  inject = context // Set the global injector
  ```
* **Injecting**: Use `inject()` lazily at class level:

  ```kotlin
  private val assetManager: AssetManager by inject()
  ```
* **Overriding Dependencies**: Screens or components may override specific bindings for local behavior. Use local context chaining if necessary.
* **Avoid Global Access**: Prefer injection over static or global references to promote testability and decoupling.
* **Injection in Screens**: Inject required services or systems in `show()` or as properties if needed immediately.
* **Avoid Manual Passing**: If a dependency is already bound, don’t pass it manually—use `inject()`.

## 5. Screen & UI Best Practices

### Screen Lifecycle Responsibilities

#### In your `Game` or `KtxGame`

* **`create()`**: Initialize global systems such as `AssetManager`, audio (music/sound), logging, and any singletons. Configure global settings (e.g., global `Viewport` defaults, default `Skin`). Bind them via `ktx.inject`.
* **`dispose()`**: Dispose of all global resources: call `assetManager.dispose()`, stop and dispose audio, and any other singletons that implement `Disposable`.

#### In each `Screen` or `KtxScreen`

* **`show()`**:

    * Instantiate and configure your `Stage` (or `SpriteBatch` and `OrthographicCamera` if not using Scene2D).
    * Inject services using `by inject()` if not already injected as properties.
    * Set up UI actors, input processors (`InputMultiplexer`), and subscribe to events or signals.
    * Trigger asset loading for screen-specific assets (textures, sounds) if not preloaded.
* **`resize(width: Int, height: Int)`**:

    * Update the camera’s viewport and call `stage.viewport.update(width, height, true)`.
    * Avoid heavy calculations; keep this method lightweight.
* **`render(delta: Float)`**:

    * Update game logic: handle input processing or pass events to controllers.
    * Advance animations or physics steps.
    * Call `stage.act(delta)` for UI, then `stage.draw()` (or `batch.begin()/batch.end()` for SpriteBatch drawing).
* **`pause()`**:

    * Save transient state (e.g., player progress, settings).
    * Pause timers, music, or any ongoing coroutines.
* **`resume()`**:

    * Resume paused music, coroutines, or animations.
    * Re-register input processors if they were cleared.
* **`hide()`**:

    * Clear input processors by calling `Gdx.input.inputProcessor = null` or removing from the multiplexer.
    * Stop listening to events related to this screen.
    * Optionally, free small temporary resources to reduce memory.
* **`dispose()`**:

    * Dispose of screen-specific `Stage`, `Skin`, textures, particle effects, and other `Disposable` objects.
    * Unsubscribe from signals or event buses to prevent memory leaks.

### Scene2D Stage

* Use `Stage` for all UI. Keep game logic strictly separate from UI rendering.
* Build UI with the `ktx.scene2d` DSL for clarity and maintainability.
* Reuse `Skin` objects across screens when possible; avoid creating new `Skin` or style instances each time.

## 6. Input Handling

* **Single InputProcessor**: Use an `InputMultiplexer` only if combining multiple processors. Register it in the active Screen’s `show()` method.
* **Encapsulation**: Delegate input logic to controllers (e.g., `PlayerInputController`) rather than scattering `Gdx.input` checks across classes.

## 7. Performance Optimization

* **SpriteBatch**: Minimize `batch.begin()`/`batch.end()` calls. Batch draw calls by texture when possible.
* **Object Pooling**: Use LibGDX’s `Pool` for frequently created objects (e.g., bullets, particle effects) to reduce GC churn.

## 8. KTX & Coroutines

* **Structured Concurrency**: Use Kotlin coroutines for asynchronous tasks (`ktx.async`). Scope them to lifecycle components (e.g., `GameScope`).
* **Cancellation**: Always cancel or complete coroutines in `dispose()` or when Screens are hidden to avoid leaks.

## 9. Testing & Debugging

* **Log Levels**: Use `ktx.log.debug/info/warn/error` appropriately. Avoid excessive logging in release builds.
~~* **Debug Flags**: Implement runtime toggles for debug rendering (e.g., show physics shapes, FPS display).~~

## 10. Documentation & Comments

* **KDoc**: Document public classes and functions using KDoc. Describe behavior, parameters, and return values.
* **TODOs & FIXMEs**: Mark with `// TODO:` or `// FIXME:` tags. Regularly triage and resolve them.
* **NOTEs**: Mark things to be mentioned in the documentation with `// NOTE:` tags.

## 11. Version Control

* **Git Flow**: Use task branches. Merge into `main` after review.

## 12. Code Reviews

* **Review**: All merge requests require at least one approving review.
* **Checklist**: Ensure adherence to these guidelines, performance considerations, and absence of console/log spam.

---

Following these guidelines will help us maintain a clean, efficient, and scalable codebase as we develop our LibGDX/KTX game. Let’s keep each other accountable and update this document as new best practices emerge.
