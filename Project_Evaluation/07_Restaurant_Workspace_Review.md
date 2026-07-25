# Restaurant Workspace Review

## Summary
The multi-tenant architecture is implemented using a "Shared Database, Isolated Records" approach. Every user and data point is linked to a unique `restaurantId`.

## Strengths
- **Isolation:** Programmatic enforcement of `restaurantId` in all queries ensures data privacy between tenants.
- **Invite Code System:** A clean, 6-digit uppercase code system for linking Staff to a Manager's workspace.
- **Scalability:** The normalized `restaurants` collection allows for future workspace-level settings (e.g., shared thresholds).

## Weaknesses
- **Invitation Lifetime:** Invite codes do not expire; once generated, they remain active forever.

## Risks
- **Security:** If an invite code is leaked, anyone can join a restaurant as Staff. 

## Evidence
- Verified in `UserRepository.kt` and `Restaurant.kt`.

## Severity
Medium (due to non-expiring invite codes).

## Recommendations
- Implement "Regenerate Invite Code" for Managers.
- Add an "Invite Approval" queue so Managers must approve a Staff member before they gain access to the Dashboard.

