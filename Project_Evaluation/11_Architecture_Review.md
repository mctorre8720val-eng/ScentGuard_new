# Architecture Review

## Summary
The project follows the standard MVVM (Model-View-ViewModel) architecture with a clean separation of concerns. It uses a Reactive pattern where the UI observes StateFlows exposed by ViewModels, which in turn interact with Repositories.

## Strengths
- **Decoupling:** Repositories abstract Firebase logic away from ViewModels, allowing for easier unit testing.
- **State Management:** `Resource<T>` sealed class is used effectively to handle Loading, Success, and Error states uniformly across the app.
- **Atomic Operations:** Registration logic is bundled into a single ViewModel action that manages sequential Firestore writes.

## Weaknesses
- **Lack of Dependency Injection (DI):** ViewModels manually instantiate repositories (e.g., `AuthRepository()`). This makes mocking repositories for unit tests difficult.
- **Hardcoded Strings:** Many UI strings are hardcoded in Composables instead of being in `strings.xml`.

## Risks
- **Scalability:** As the app grows, the lack of a DI framework like Hilt will lead to boilerplate-heavy ViewModel factories and manual dependency passing.

## Evidence
- Verified in `RegistrationViewModel.kt` and `MainViewModel.kt`.

## Severity
Medium (due to lack of DI and string resources).

## Recommendations
- Integrate **Hilt** or **Koin** for Dependency Injection.
- Migrate all hardcoded strings to `res/values/strings.xml` to support future localization.

