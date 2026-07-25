# Security Review

## Summary
The application relies on Firebase Authentication for identity and client-side role checks for access control. Data isolation is maintained via `restaurantId`.

## Strengths
- **Role Enforcement:** Critical screens (`StaffScreen`, `ReportsScreen`) have internal logic to boot non-authorized users.
- **Data Isolation:** All repository methods require a UID or RestaurantID to fetch data.
- **No Secrets:** No API keys or sensitive hardcoded strings found in the codebase.

## Weaknesses
- **Client-Side Reliance:** Role validation is performed at the software level. While effective, it must be complemented by Firestore Security Rules for true security.

## Risks
- **Insecure Direct Object References (IDOR):** If Firestore rules are not set to check if `request.auth.uid == resource.data.managerUid`, a malicious user could theoretically query other restaurants' data via the API.

## Evidence
- Verified in `UserRepository.kt` and Screen composables.

## Severity
High (if Firestore Rules are not properly configured on the backend).

## Recommendations
- Document and verify Firestore Security Rules to match the multi-tenant architecture (e.g., allow read if `resource.data.restaurantId == request.auth.token.restaurantId`).
- Use Firebase Auth Custom Claims for the `role` to prevent client-side spoofing.

