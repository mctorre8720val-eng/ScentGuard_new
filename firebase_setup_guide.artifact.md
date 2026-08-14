# Master Backend Setup Guide (From Step 1)

This guide will walk you through setting up a completely new Firebase backend and configuring the necessary Google Cloud APIs for ScentGuard.

## Step 1: Create a Fresh Firebase Project
1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add Project**.
3. Name it **"ScentGuard-New"** (or your preferred name).
4. Disable or Enable Google Analytics (it doesn't affect core logic).
5. Click **Create Project** and wait for it to finish.

---

## Step 2: Configure Authentication (Google Sign-In)
1. In the left sidebar, click **Build > Authentication**.
2. Click **Get Started**.
3. Under **Sign-in method**, click **Add new provider**.
4. Select **Google**.
5. Enable the toggle.
6. Choose a **Project support email** from the dropdown.
7. **CRITICAL:** Note the **Web SDK configuration** section. You will see a **Web Client ID**.
   - Copy this ID and save it. We will need it in the code later.
8. Click **Save**.

---

## Step 3: Register the Android App
1. Go to **Project Settings** (the gear icon next to Project Overview).
2. Scroll down to **Your apps** and click **Add app > Android**.
3. **Package Name:** `com.example.scentguard` (Must match exactly).
4. **App Nickname:** ScentGuard Dev.
5. **SHA-1 Fingerprint:**
   - In Android Studio, open the **Terminal** tab at the bottom.
   - Run this command:
     ```bash
     ./gradlew signingReport
     ```
   - Scroll up to find the `debug` variant. Copy the `SHA1` string.
   - Paste it into the Firebase **Debug signing certificate SHA-1** field.
6. Click **Register app**.
7. Download the **google-services.json** and move it into your project's `app/` folder.

---

## Step 4: Setup Firestore Database
1. In the sidebar, click **Build > Firestore Database**.
2. Click **Create database**.
3. Choose **Start in test mode** for now.
4. Pick a location (e.g., `us-central`).
5. Once created, go to the **Rules** tab and paste these to fix "Permission Denied" errors:
   ```javascript
   service cloud.firestore {
     match /databases/{database}/documents {
       // Profile rule: Users can only read/write their own UID doc
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       // Shared rule: Authenticated users can manage restaurants
       match /restaurants/{restaurantId} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```
6. Click **Publish**.

---

## Step 5: Enable Google Cloud APIs
Some services require extra activation in the [Google Cloud Console](https://console.cloud.google.com/):
1. Select your project in the top dropdown.
2. Go to **APIs & Services > Library**.
3. Search for and **Enable** these:
   - **Google People API** (Required for some Google Auth features).
   - **Cloud Firestore API** (Should be on by default, but verify).

---

## Summary of Data Needed for Code
Once you are done, I need you to provide me with:
1. **Web Client ID** (From Step 2).
2. Confirmation that the **google-services.json** is in the `app/` folder.

---
> [!TIP]
> Once you have the **Web Client ID**, just type it here and say "Ready for implementation!"
