# Authentication Review

## Summary
The authentication system is highly robust, using Firebase Auth and Firestore. It implements an atomic registration flow that ensures data consistency across three distinct entities: Auth User, Restaurant Document, and User Profile.

## Strengths
- **Atomicity:** Sequential creation of documents ensures no "orphaned" accounts are created without a link to a restaurant.
- **Validation:** Real-time email validation and 6-digit invite code verification for Staff.
- **Feedback:** Clear "Status Messages" during the multi-step registration process (e.g., "Setting up restaurant workspace...").
- **Persistence:** Reliable session persistence and auto-login logic in the `SplashScreen`.

## Weaknesses
- **Error Mapping:** While most errors are caught, some raw Firebase exception messages may still leak to the UI if not perfectly mapped in the `RegistrationViewModel`.

## Risks
- **Account Recovery:** No "Delete Account" option in the UI, which may be required by GDPR/App Store guidelines.

## Evidence
- Verified in `AuthRepository.kt`, `RegistrationViewModel.kt`, and `LoginScreen.kt`.

## Severity
Low.

## Recommendations
- Implement a "Delete Account" flow that cleans up both Auth and Firestore data.
- Standardize all error messages into localized string resources.

