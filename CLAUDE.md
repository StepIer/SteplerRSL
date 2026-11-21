# SteplerRSL — development rules

Android app, Kotlin, single Gradle module `:app`, base package `com.stepler.steplerrsl`.

## Package structure: core / common / feature

The app is split by **feature first**, then by Clean Architecture layer inside each feature.

```
com.stepler.steplerrsl
├── SteplerApp.kt, MainActivity.kt
├── core/                   # foundation of the app, knows nothing about features
│   ├── di/                 #   app-wide Hilt modules (network, database, dispatchers)
│   ├── network/            #   HTTP client setup, interceptors, base API error handling
│   ├── database/           #   Room database, migrations
│   ├── datastore/          #   preferences storage
│   ├── navigation/         #   NavHost, route definitions, navigation helpers
│   ├── ui/theme/           #   design system: theme, colors, typography, dimensions
│   └── util/               #   dispatchers, Result helpers, extensions
├── common/                 # pieces shared by 2+ features
│   ├── data/               #   shared repository impls, DTOs, mappers
│   ├── domain/             #   shared models, repository interfaces, use cases
│   └── presentation/       #   reusable composables (buttons, dialogs, loaders, error views)
└── feature/
    └── <featurename>/      # one package per feature, e.g. feature/steps, feature/profile
        ├── data/
        │   ├── remote/     #   DTOs + API services
        │   ├── local/      #   DB entities, DAOs
        │   ├── mapper/     #   DTO/Entity <-> domain model
        │   └── repository/ #   XxxRepositoryImpl : XxxRepository
        ├── domain/
        │   ├── model/      #   domain models (plain data classes)
        │   ├── repository/ #   repository interfaces
        │   └── usecase/    #   one use case = one business action
        ├── presentation/   #   XxxScreen.kt, XxxContent.kt, XxxViewModel.kt, XxxState.kt, XxxSideEffect.kt
        └── di/             #   Hilt module binding this feature's repositories
```

### What goes where
- **`core`** — infrastructure every part of the app stands on and that has no business meaning: DI setup, network/DB clients, navigation host, design system, generic utilities. If it would exist in any app, it's `core`.
- **`common`** — business logic or UI that is **used by two or more features** (e.g. a `User` model and `GetCurrentUserUseCase` used by both `profile` and `steps`). Follows the same `data/domain/presentation` split. Code starts in a feature and moves to `common` only once a second feature needs it.
- **`feature/<name>`** — everything specific to one feature. A new screen/flow = a new feature package. Package names are lowercase, single word where possible.

### Dependency rules between packages
- `feature` → `common` → `core`. Arrows only point that way.
- **Features never import from other features.** If two features need the same thing, move it to `common`. Cross-feature navigation goes through routes defined in `core/navigation`, passing only IDs/primitives.
- `common` never imports from `feature`. `core` never imports from `common` or `feature`.

### Dependency rule between layers (inside every feature and `common`)
- `presentation` → `domain` ← `data`. Dependencies point **inward to `domain` only**.
- `domain` must not import anything from `data`, `presentation`, `android.*` or `androidx.*`. Only Kotlin stdlib and `kotlinx.coroutines`.
- `presentation` never touches `data` classes (DTOs, entities, API services, DAOs) directly — only use cases and domain models.
- `data` implements the interfaces declared in `domain/repository`.
- Each layer has its own models; map at the boundary (`data/mapper`, and UI-specific models in `presentation` if needed). Never expose DTOs or DB entities beyond `data`.

### Domain layer
- Use cases are named `VerbNounUseCase` (e.g. `GetStepsUseCase`) and expose a single `operator fun invoke(...)`.
- Use `suspend` functions for one-shot operations and `Flow` for streams.
- Errors are returned as `Result<T>` (or a sealed domain error type) — don't let raw network/DB exceptions leak to presentation.
- Depend on interfaces through constructor injection, never instantiate dependencies inside a class.

### Data layer
- Repository implementations decide the data source (remote/local/cache) and switch to `Dispatchers.IO` themselves; callers never manage threading.
- Catch infrastructure exceptions here and map them to domain results/errors.

## Presentation: Jetpack Compose + Orbit MVI

### UI
- All UI is Jetpack Compose with Material 3. No XML layouts, no Fragments.
- Split each screen into:
  - `XxxScreen(viewModel)` — stateful route: gets the ViewModel, collects state and side effects.
  - `XxxContent(state, onAction...)` — stateless composable that only renders state and forwards events. Add `@Preview`s for this one.
- Composables receive state + lambdas; they never call use cases or hold business logic.
- Use the theme from `core/ui/theme` (`MaterialTheme.colorScheme`, `typography`); no hard-coded colors or text sizes. Strings go in `res/values/strings.xml`.

### ViewModel with Orbit MVI
Every ViewModel is an Orbit `ContainerHost<State, SideEffect>`:

```kotlin
data class StepsState(
    val isLoading: Boolean = false,
    val steps: List<Step> = emptyList(),
    val error: String? = null,
)

sealed interface StepsSideEffect {
    data class ShowMessage(val text: String) : StepsSideEffect
    data object NavigateBack : StepsSideEffect
}

@HiltViewModel
class StepsViewModel @Inject constructor(
    private val getStepsUseCase: GetStepsUseCase,
) : ViewModel(), ContainerHost<StepsState, StepsSideEffect> {

    override val container = container<StepsState, StepsSideEffect>(StepsState()) {
        loadSteps()
    }

    fun loadSteps() = intent {
        reduce { state.copy(isLoading = true, error = null) }
        getStepsUseCase()
            .onSuccess { steps -> reduce { state.copy(isLoading = false, steps = steps) } }
            .onFailure { e ->
                reduce { state.copy(isLoading = false, error = e.message) }
                postSideEffect(StepsSideEffect.ShowMessage(e.message.orEmpty()))
            }
    }

    fun onBackClick() = intent { postSideEffect(StepsSideEffect.NavigateBack) }
}
```

Rules:
- **State** is an immutable `data class` with defaults for every field; it's the single source of truth for the screen. Change it only inside `reduce { state.copy(...) }`.
- **Side effects** are a `sealed interface` for one-time events (navigation, snackbars/toasts). Never put one-time events in state.
- Every user action is a public ViewModel function that returns `intent { ... }`. No public `MutableStateFlow`/`LiveData`.
- ViewModels depend on **use cases only**, never on repositories or data sources.
- Use `repeatOnSubscription { }` inside `intent` for flows that should run only while the UI is collecting.

Collect in Compose with `orbit-compose`:

```kotlin
@Composable
fun StepsScreen(onBack: () -> Unit, viewModel: StepsViewModel = hiltViewModel()) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is StepsSideEffect.ShowMessage -> { /* show snackbar */ }
            StepsSideEffect.NavigateBack -> onBack()
        }
    }
    StepsContent(state = state, onRetry = viewModel::loadSteps)
}
```

## Dependencies
- Add every library to `gradle/libs.versions.toml` and reference it as `libs.xxx`; no hard-coded versions in `build.gradle.kts`.
- Orbit: `orbit-viewmodel`, `orbit-compose`, and `orbit-test` for tests.

## Dependency injection: Hilt
- `SteplerApp` is the `@HiltAndroidApp` application; activities are `@AndroidEntryPoint`.
- ViewModels are `@HiltViewModel` with `@Inject constructor`; obtain them in Compose with `hiltViewModel()`.
- Use cases and repository impls use `@Inject constructor` — no manual instantiation.
- App-wide Hilt modules live in `core/di`; each feature binds its own repositories in `feature/<name>/di`; shared bindings go in `common/di`. Modules are annotated `@Module @InstallIn(SingletonComponent::class)`. Bind repository interfaces with `@Binds` (e.g. `bindStepsRepository(impl: StepsRepositoryImpl): StepsRepository`); use `@Provides` only for third-party objects (Retrofit, Room, etc.).
- `domain` stays free of Hilt/Android annotations except `javax.inject.Inject` on constructors.
- Annotation processing uses KSP (`ksp(...)`), never kapt.

## Testing
- Domain: plain JUnit tests for use cases with fake repositories.
- ViewModels: test with `orbit-test` (`viewModel.test(this) { expectState { ... }; expectSideEffect(...) }`), faking use cases.
- Data: test mappers and repository logic against fake data sources.

## Conventions
- Kotlin official code style. One public top-level class per file, file named after it.
- Naming: `XxxViewModel`, `XxxState`, `XxxSideEffect`, `XxxScreen`, `XxxUseCase`, `XxxRepository` / `XxxRepositoryImpl`, `XxxDto`, `XxxEntity`.
- Build check before committing: `./gradlew assembleDebug testDebugUnitTest`.
