const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Cloud Function to trigger FCM notifications when airStatus changes to DANGER.
 * Path: restaurants/{restaurantId}
 */
exports.onAirStatusUpdate = functions.firestore
    .document("restaurants/{restaurantId}")
    .onUpdate(async (change, context) => {
        const beforeData = change.before.data();
        const afterData = change.after.data();
        const restaurantId = context.params.restaurantId;

        // 1. Check if the status transitioned to DANGER
        // Logic: (Not Danger) -> (Danger)
        const wasDanger = beforeData.airStatus === "DANGER";
        const isNowDanger = afterData.airStatus === "DANGER";

        if (!wasDanger && isNowDanger) {
            console.log(`DANGER status detected for restaurant: \${restaurantId}`);

            const gasPpm = afterData.currentGasPpm || 0;
            const restaurantName = afterData.name || "Restaurant";

            // 2. Query all users belonging to this restaurant
            const usersSnapshot = await admin.firestore()
                .collection("users")
                .where("restaurantId", "==", restaurantId)
                .get();

            const tokens = [];
            usersSnapshot.forEach(doc => {
                const userData = doc.data();
                if (userData.fcmToken) {
                    tokens.push(userData.fcmToken);
                }
            });

            if (tokens.length === 0) {
                console.log("No FCM tokens found for this restaurant.");
                return null;
            }

            // 3. Construct the message
            const message = {
                notification: {
                    title: "ScentGuard Alert",
                    body: `Dangerous gas level detected in \${restaurantName}. Reading: \${gasPpm} ppm. Check the storage room immediately.`,
                },
                data: {
                    type: "DANGER_ALERT",
                    restaurantId: restaurantId,
                    gasPpm: gasPpm.toString()
                },
                tokens: tokens,
            };

            // 4. Send the notification
            const response = await admin.messaging().sendEachForMulticast(message);
            console.log(`Successfully sent \${response.successCount} notifications.`);

            // 5. Cleanup invalid tokens
            if (response.failureCount > 0) {
                const failedTokens = [];
                response.responses.forEach((resp, idx) => {
                    if (!resp.success) {
                        failedTokens.push(tokens[idx]);
                    }
                });
                console.log(`Cleaning up \${failedTokens.length} failed tokens.`);
                // Optional: add logic here to remove these tokens from the user documents
            }

            return null;
        }

        return null;
    });
