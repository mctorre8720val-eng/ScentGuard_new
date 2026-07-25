# Prioritized Recommendations

## 🚨 Critical (Immediate Action)
1. **Firestore Rules Audit:** Explicitly write and test rules in the Firebase Console to ensure `restaurantId` isolation is enforced at the server level, not just the client.
2. **Dependency Injection:** Integrate Hilt to stabilize the architecture before Phase 2 adds more complexity.

## 📈 High Priority (Before Phase 2 End)
3. **Strings.xml Migration:** Move all hardcoded text to string resources to ensure maintainability.
4. **Offline Caching:** Implement local caching for the User Profile to allow the app to open even without immediate internet.

## 🛠️ Medium Priority (Before Phase 3)
5. **Invite Management:** Add "Invite Expiration" and "Regenerate Code" features for enhanced workspace security.
6. **Deep Linking:** Prepare navigation routes for push notification integration.

## 🎨 Low Priority (Polish)
7. **Dashboard Optimization:** Refine the hero section layout for small-screen devices.

