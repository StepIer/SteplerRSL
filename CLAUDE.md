# SteplerRSL — development rules

Android app, Kotlin, single Gradle module `:app`, base package `com.stepler.steplerrsl`.

## Architecture: Clean Architecture, 3 layers

```
com.stepler.steplerrsl
├── data/            # implementations: API, DB, DataStore, mappers, repository impls
│   ├── remote/      #   network DTOs + API services
│   ├── local/       #   DB entities, DAOs, preferences
│   ├── mapper/      #   DTO/Entity <-> domain model mappers
│   └── repository/  #   XxxRepositoryImpl : XxxRepository
├── domain/          # pure Kotlin business logic
│   ├── model/       #   domain models (plain data classes)
│   ├── repository/  #   repository interfaces
│   └── usecase/     #   one use case = one business action
└── presentation/    # UI
    ├── <feature>/   #   XxxScreen.kt, XxxViewModel.kt, XxxState.kt, XxxSideEffect.kt
    ├── navigation/
    └── ui/theme/    #   Compose theme, colors, typography
```

### Dependency rule
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
- Use the theme from `presentation/ui/theme` (`MaterialTheme.colorScheme`, `typography`); no hard-coded colors or text sizes. Strings go in `res/values/strings.xml`.

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
- Hilt modules live in a `di/` package (`com.stepler.steplerrsl.di`), annotated `@Module @InstallIn(SingletonComponent::class)`. Bind repository interfaces with `@Binds` (e.g. `bindStepsRepository(impl: StepsRepositoryImpl): StepsRepository`); use `@Provides` only for third-party objects (Retrofit, Room, etc.).
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
