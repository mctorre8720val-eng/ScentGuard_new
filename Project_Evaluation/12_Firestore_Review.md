# Firestore Structure Review

## Summary
The database schema is normalized and follows NoSQL best practices. It uses two primary collections: `users` and `restaurants`.

## Strengths
- **Normalization:** Linking users to restaurants via `restaurantId` instead of simple name strings.
- **Performance Optimization:** `restaurantName` is duplicated in the `User` document (Denormalization) to avoid extra joins for simple UI display.
- **Scalability:** The structure easily supports future collections like `sensor_data` or `maintenance_logs` under a restaurant-specific hierarchy.

## Weaknesses
- **Missing Sub-collections:** Logs and notifications are currently mock. In production, these should likely be sub-collections of a `Restaurant` to maintain isolation.

## Risks
- **Index Management:** Complex queries (e.g., filtering staff by restaurant AND role) will require manual index creation in the Firebase Console.

## Evidence
- Verified in `UserRepository.kt` and `User.kt`.

## Severity
Low.

## Recommendations
- Plan for a `logs` sub-collection under `restaurants/{id}` for high-volume sensor data to optimize query costs.

