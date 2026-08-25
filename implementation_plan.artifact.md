# Implementation Plan - Add Google Icon to Login Button

This plan details the addition of the official Google "G" icon to the existing Google Sign-In button on the Login screen, ensuring a professional and branded appearance without altering any underlying logic.

## User Review Required

> [!NOTE]
> I am adding a new vector resource `ic_google_logo.xml` and updating the `GoogleButton` component to display it. No functional changes will be made to the authentication flow.

## Proposed Changes

### 1. Resources
#### [NEW] [ic_google_logo.xml](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/res/drawable/ic_google_logo.xml)
- Add a new vector drawable containing the official Google "G" logo with its four brand colors (Red, Yellow, Green, Blue).

### 2. UI Components
#### [MODIFY] [GoogleButton.kt](file:///Users/michaelangelotorre/StudioProjects/ScentGuard_new/app/src/main/java/com/example/scentguard/ui/components/GoogleButton.kt)
- Update the button content to include an `Image` composable before the "Sign in with Google" text.
- Use `Row` for proper horizontal alignment and spacing (12dp) between the icon and the label.

---

## Verification Plan

### Manual Verification
- **Visual Check:** Open the Login screen and verify that the "Sign in with Google" button now displays the multi-colored Google "G" icon.
- **Loading State:** Ensure the icon is replaced by the `CircularProgressIndicator` when the button is in its loading state.
- **Functionality:** Click the button to confirm it still triggers the Google account picker as expected.
