# MCP.md

# ScentGuard Vent

## Project Overview

ScentGuard Vent is an IoT-based smart ventilation system designed for small to medium-sized restaurant garbage storage rooms. The system monitors indoor air quality by detecting gases produced from waste decomposition and automatically activates ventilation when gas concentration thresholds indicating poor air quality are exceeded.

The mobile application is developed using **Kotlin**, **Jetpack Compose**, **Material Design 3**, and **Firebase**.

This project follows a phased development approach to ensure scalability, maintainability, and clean software architecture.

The project is being developed in multiple phases. Each phase builds upon the previous one without requiring major architectural changes.

---

# Current Development Phase

## Phase 1 — Mobile Application Foundation

The current objective is to complete the Android application foundation before integrating any IoT hardware.

Only the mobile application, Firebase Authentication, Cloud Firestore, and role-based UI should be implemented during this phase.

All sensor-related information displayed inside the application must use realistic placeholder values.

### Phase 1 Scope

Implement:

- Splash Screen
- Login
- Sign Up
- Forgot Password
- Persistent Login
- Logout
- Firebase Authentication
- Cloud Firestore
- User Profiles
- Manager & Staff Roles
- Role-Based Navigation
- Dashboard
- Notifications Page (Placeholder)
- History Page (Placeholder)
- Reports Page (Placeholder)
- Settings Page (Placeholder)
- Profile Page
- Material Design 3 UI
- Light & Dark Mode
- MVVM Architecture

Do NOT implement hardware communication during this phase.

---

# Development Roadmap

## Phase 1 — Mobile Application

Focus:

- Android UI
- Authentication
- Firestore
- Navigation
- User Roles

Status:

🟢 Current Development Phase

---

## Phase 2 — IoT Integration

Focus:

- ESP32
- Gas Sensors
- Firebase Realtime Database
- Live Sensor Monitoring
- Automatic Ventilation
- Manual Fan Control (Manager Only)

---

## Phase 3 — Monitoring & Analytics

Focus:

- Push Notifications
- Alert History
- Historical Logs
- Daily Reports
- Weekly Reports
- Monthly Reports
- Charts
- Analytics
- Maintenance Logs

---

## Phase 4 — Finalization

Focus:

- Performance Optimization
- Error Handling
- Sensor Calibration
- User Experience Improvements
- Documentation
- Final Testing
- Production Build

---

# Technology Stack

## Mobile

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM Architecture
- Compose Navigation
- Kotlin Coroutines
- StateFlow

---

## Firebase

Current Phase

- Firebase Authentication
- Cloud Firestore

Future

- Firebase Realtime Database
- Firebase Cloud Messaging

---

## Hardware

Future Implementation

- ESP32
- MQ-Series Gas Sensors
- Relay Module
- Exhaust Fan
- Temperature & Humidity Sensor (DHT22 or equivalent)

---

# Project Architecture

The project follows MVVM Architecture.

```
UI
        ↓
ViewModel
        ↓
Repository
        ↓
Firebase
```

Business logic should never exist inside Composables.

Firebase operations must remain inside Repository classes.

ViewModels should manage UI state using StateFlow.

---

# Future Architecture

```
Gas Sensors
        ↓
ESP32
        ↓
Wi-Fi
        ↓
Firebase Realtime Database
        ↓
Repository
        ↓
ViewModel
        ↓
Dashboard
```

The existing dashboard should not require major modifications when moving from mock data to live ESP32 data.

---

# Design Principles

The application should be:

- Modern
- Professional
- Minimal
- Responsive
- Clean
- Scalable
- Maintainable

Follow Material Design 3 guidelines.

Use reusable Jetpack Compose components whenever possible.

Avoid duplicate code.

Follow SOLID principles whenever applicable.

---

# Current AI Focus

The AI should currently focus ONLY on Phase 1.

Current priorities:

- Authentication
- Firestore
- User Profiles
- Navigation
- Dashboard UI
- Role-Based Access
- Clean Architecture

The AI should ignore all IoT implementation until Phase 2 begins.


---

# Application Flow

The application follows the authentication flow below.

```
Splash Screen
        ↓
Check Authentication State
        ↓
If Authenticated
        ↓
Retrieve User Profile
        ↓
Retrieve User Role
        ↓
Dashboard

OR

Splash Screen
        ↓
Not Authenticated
        ↓
Login
        ↕
Sign Up
```

The Splash Screen should automatically determine whether a user is already authenticated.

If the user is authenticated:

1. Retrieve the user's profile from Cloud Firestore.
2. Retrieve the user's role.
3. Navigate to the Dashboard.

If the user is not authenticated:

Navigate to the Login screen.

The application should use **one shared Dashboard**.

Do **not** create separate dashboard layouts for Manager and Staff.

Instead, use role-based rendering to show or hide UI elements depending on the authenticated user's role.

---

# Required Screens

The application should contain the following screens.

## Splash Screen

Purpose:

Determine the user's authentication state and display application branding.

Requirements:

- ScentGuard Vent logo
- Application name
- Tagline

> Smart Air Quality Monitoring for Restaurant Waste Storage

Features:

- Fade or Scale animation
- Check Firebase Authentication state
- Automatically navigate to Login or Dashboard

---

## Login Screen

Implement Firebase Authentication login.

Features:

- Email Address
- Password
- Show / Hide Password
- Forgot Password
- Login Button
- Navigate to Sign Up

Validation:

- Empty fields
- Invalid email format
- Incorrect credentials
- Friendly error messages

Persistent login should be supported.

---

## Sign Up Screen

Implement Firebase Authentication registration.

Fields:

- Full Name
- Restaurant Name
- Email Address
- Password
- Confirm Password
- User Role

Available Roles:

- Manager
- Staff

Validation:

- Required fields
- Valid email
- Password strength
- Password confirmation
- Selected role

After successful registration:

1. Create Firebase Authentication account.
2. Save user profile to Cloud Firestore.
3. Save selected role.
4. Navigate to Dashboard.

---

## Registration Rules

For Phase 1, users may select either role during registration.

Available roles:

- Manager
- Staff

The selected role must be stored in Cloud Firestore.

This behavior exists for development and testing.

Future versions may restrict Manager account creation to authorized administrators.

---

## Dashboard

There should only be **one Dashboard**.

Do not create separate dashboards for Manager and Staff.

Instead:

- Show or hide navigation items.
- Restrict actions using role-based access.
- Keep one reusable dashboard implementation.

The Dashboard should use **mock data only** during Phase 1.

---

### Dashboard Header

Display:

- Greeting
- User Full Name
- Restaurant Name
- Profile Picture (placeholder)
- Current Role

Example:

```
Good Morning

Juan Dela Cruz
ABC Restaurant

Role:
Manager
```

---

### Air Quality Card

Display placeholder values.

Example:

```
Air Quality

Good
```

Display:

- Air Quality Status
- Gas Concentration (ppm)
- Last Updated

Example:

```
Gas Concentration

185 ppm

Last Updated

Just now
```

---

### Ventilation Card

Display placeholder information.

Fields:

- Fan Status
- Automatic Mode

Example:

```
Fan Status

OFF

Automatic Mode

Enabled
```

---

### Environmental Card

Display placeholder values.

Fields:

- Temperature
- Humidity

Example:

```
Temperature

28°C

Humidity

71%
```

---

### Statistics

Display placeholder statistics.

Cards:

- Alerts Today
- Fan Runtime
- Sensor Status

Example:

```
Alerts Today

2

Fan Runtime

35 min

Sensor Status

Online
```

---

### Recent Activity

Populate with realistic placeholder events.

Example:

- Fan Activated
- Air Quality Returned to Normal
- High Gas Concentration Detected
- Ventilation Cycle Completed

---

### Quick Actions

Manager

- Dashboard
- History
- Reports
- Notifications
- Settings

Staff

- Dashboard
- Notifications
- Profile

Manager-only items should automatically be hidden from Staff users.

---

# Firebase

## Authentication

Phase 1 should implement:

- Email & Password Registration
- Email & Password Login
- Forgot Password
- Logout
- Persistent Login

Authentication should use Firebase Authentication.

---

## Cloud Firestore

Phase 1 should implement only the **users** collection.

Collection:

```
users
```

Document ID:

```
uid
```

Fields:

```
fullName
restaurantName
email
role
createdAt
```

Example:

```
users

    Xh2As82...

        fullName:
            Juan Dela Cruz

        restaurantName:
            ABC Restaurant

        email:
            juan@email.com

        role:
            manager

        createdAt:
            Firebase Timestamp
```

Future collections (do NOT implement yet):

```
history

alerts

sensorData

reports

settings
```

These collections belong to future development phases.

---

# User Roles

The application supports two user roles.

## Manager

Managers have complete access to the application.

Current Phase Permissions

- Login
- Register
- Dashboard
- Notifications
- History (Placeholder)
- Reports (Placeholder)
- Settings (Placeholder)
- Profile
- Logout

Future Permissions

- Manual Fan Control
- Configure Automatic Ventilation
- Sensor Management
- View Live Sensor Data
- Device Management

---

## Staff

Staff have limited access.

Current Phase Permissions

- Login
- Dashboard
- Notifications
- Profile
- Logout

Future Permissions

- View Air Quality
- View Fan Status
- Receive Notifications

Staff cannot:

- Access Reports
- Access Settings
- Configure the ventilation system
- Control the fan
- Manage devices
- Modify system configurations

Role-based permissions should always be enforced through the authenticated user's Firestore role.


---

# Project Folder Structure

The project should follow a clean and scalable MVVM architecture.

```
ScentGuard/

├── app/
│
├── data/
│   ├── firebase/
│   ├── model/
│   ├── repository/
│   └── local/
│
├── navigation/
│
├── ui/
│   ├── components/
│   ├── screens/
│   │   ├── splash/
│   │   ├── login/
│   │   ├── signup/
│   │   ├── dashboard/
│   │   ├── history/
│   │   ├── reports/
│   │   ├── notifications/
│   │   ├── settings/
│   │   └── profile/
│   │
│   └── theme/
│
├── viewmodel/
│
├── utils/
│
├── MainActivity.kt
│
└── ScentGuardApplication.kt
```

All files should be organized according to their responsibility.

Avoid placing unrelated files inside the same package.

---

# MVVM Architecture Rules

The application must strictly follow MVVM Architecture.

```
UI
        ↓
ViewModel
        ↓
Repository
        ↓
Firebase
```

Responsibilities:

### UI

Responsible only for:

- Displaying data
- Collecting user input
- Navigation
- Calling ViewModels

The UI should never directly communicate with Firebase.

---

### ViewModel

Responsible for:

- Managing UI State
- Calling repositories
- Validation
- Business logic
- Error handling

Use StateFlow to expose UI state.

Do not place Firebase logic inside ViewModels.

---

### Repository

Responsible for:

- Firebase Authentication
- Cloud Firestore
- Future Realtime Database
- Future local storage

Repositories act as the single source of truth for data.

---

### Firebase Layer

Responsible only for:

- Authentication
- Database operations

No UI logic should exist inside Firebase classes.

---

# State Management

Use Kotlin StateFlow.

Avoid using mutable variables directly inside Composables.

Expose immutable StateFlow objects from ViewModels.

Example flow:

```
Firebase

↓

Repository

↓

ViewModel

↓

StateFlow

↓

Composable UI
```

---

# Navigation

Use Jetpack Compose Navigation.

Create one centralized navigation graph.

Avoid deeply nested navigation graphs unless necessary.

Navigation should support:

- Splash
- Login
- Sign Up
- Dashboard
- Notifications
- History
- Reports
- Settings
- Profile

Future screens should integrate without requiring major changes.

---

# UI Guidelines

The application should look modern and professional.

Design Principles:

- Material Design 3
- Minimalist
- Responsive
- Consistent spacing
- Rounded cards
- Clear typography
- Restaurant-oriented color palette

Support:

- Light Mode
- Dark Mode

Avoid cluttered layouts.

---

# Reusable Components

Create reusable Jetpack Compose components whenever possible.

Examples:

- Primary Button
- Secondary Button
- App TextField
- Password TextField
- Dashboard Card
- Statistic Card
- Profile Header
- Section Title
- Confirmation Dialog
- Loading Indicator
- Empty State
- Error Message

Avoid duplicated UI code.

---

# Error Handling

Provide friendly error messages.

Examples:

Authentication

- Invalid email
- Incorrect password
- Account not found
- Email already exists

Validation

- Required field
- Password mismatch
- Weak password

Network

- No internet connection
- Firebase unavailable

Avoid exposing raw Firebase exceptions to users.

---

# Coding Standards

Always follow:

- MVVM Architecture
- Repository Pattern
- SOLID Principles
- Clean Code
- DRY (Don't Repeat Yourself)
- Kotlin Best Practices

Use:

- Coroutines
- StateFlow
- Immutable UI State
- ViewModels

Avoid:

- Business logic inside Activities
- Business logic inside Composables
- Hardcoded strings
- Duplicate code
- Large Composable functions

Prefer small, reusable functions.

---

# Naming Conventions

Classes

```
LoginViewModel
DashboardRepository
UserRepository
DashboardScreen
ProfileScreen
```

Functions

```
login()

logout()

register()

getCurrentUser()

saveUserProfile()

loadDashboard()
```

Variables

```
userRepository

authState

dashboardUiState

currentUser
```

Use descriptive names.

Avoid abbreviations.

---

# Dependency Management

Only add dependencies that are necessary.

Current Phase libraries should include:

- Jetpack Compose
- Material Design 3
- Navigation Compose
- Firebase Authentication
- Cloud Firestore
- Kotlin Coroutines
- Lifecycle ViewModel
- StateFlow

Do not add unnecessary third-party libraries.

---

# AI Development Rules

When generating code:

- Write production-quality Kotlin.
- Follow MVVM Architecture.
- Keep files small and maintainable.
- Prefer reusable components.
- Organize files according to the project structure.
- Follow Material Design 3 guidelines.
- Separate UI, business logic, and data access.
- Write scalable code that supports future IoT integration.

Whenever possible:

- Reuse existing components.
- Avoid code duplication.
- Follow Kotlin best practices.
- Keep architecture consistent.

Never break the existing project structure.

---

# Current AI Priorities

During Phase 1, focus ONLY on:

- Splash Screen
- Login
- Sign Up
- Dashboard
- Firebase Authentication
- Cloud Firestore
- User Profiles
- Role-Based Access
- Navigation
- UI Polish

Everything should be production-ready.

All IoT functionality must remain as placeholder data until Phase 2.

---

# DO NOT IMPLEMENT YET

The following features belong to future development phases and **must NOT** be implemented during Phase 1.

The goal of Phase 1 is to establish a stable mobile application foundation using Firebase Authentication and Cloud Firestore only.

---

## Hardware Integration

Do NOT implement:

- ESP32 communication
- Device pairing
- Wi-Fi communication with ESP32
- Serial communication
- Bluetooth communication
- Relay communication
- Hardware configuration
- Firmware-related functionality

---

## Sensor Integration

Do NOT implement:

- MQ gas sensor readings
- Temperature sensor readings
- Humidity sensor readings
- Sensor calibration
- Sensor diagnostics
- Sensor health monitoring
- Air quality computation using live hardware

All displayed sensor values must use realistic placeholder data.

---

## Firebase Realtime Database

Do NOT implement:

- Firebase Realtime Database
- Live sensor synchronization
- Real-time listeners
- Streaming sensor data
- Live dashboard updates

Only Cloud Firestore should be used during Phase 1.

---

## Ventilation System

Do NOT implement:

- Automatic fan activation
- Automatic fan deactivation
- Relay control
- Manual fan controls
- Threshold detection
- Automatic ventilation logic

Manager controls for these features will be added during Phase 2.

---

## Notifications

Do NOT implement:

- Firebase Cloud Messaging
- Push notifications
- Sensor-triggered alerts
- Scheduled notifications
- Notification history

The Notifications screen may display placeholder content.

---

## Reports & Analytics

Do NOT implement:

- Charts
- Graphs
- Historical reports
- Daily reports
- Weekly reports
- Monthly reports
- Air quality analytics
- Fan runtime analytics
- Data export

Reports should remain placeholder pages during Phase 1.

---

## History

Do NOT implement:

- Historical gas logs
- Historical fan activity
- Historical temperature logs
- Historical humidity logs

The History screen may contain placeholder UI only.

---

# Placeholder Data Rules

During Phase 1, every IoT-related value displayed in the application must be generated using realistic mock data.

Examples:

```
Air Quality

Good
```

```
Gas Concentration

185 ppm
```

```
Temperature

28°C
```

```
Humidity

71%
```

```
Fan Status

OFF
```

```
Automatic Mode

Enabled
```

These values should never originate from hardware during Phase 1.

The architecture should make it easy to replace placeholder values with live Firebase Realtime Database data during Phase 2.

---

# Future Development Rules

Future implementations should extend the existing architecture rather than replace it.

When Phase 2 begins:

- Replace mock data with live data.
- Preserve the existing Dashboard UI.
- Preserve navigation.
- Preserve ViewModels.
- Preserve repositories whenever possible.

The goal is to minimize refactoring.

---

# Performance Guidelines

The application should remain:

- Fast
- Responsive
- Stable
- Maintainable

Avoid:

- Unnecessary recompositions
- Duplicate API calls
- Memory leaks
- Long-running operations on the main thread

Always use Coroutines for asynchronous operations.

---

# Security Guidelines

Follow Firebase security best practices.

Never:

- Store passwords in Firestore.
- Hardcode Firebase credentials.
- Expose sensitive information.
- Store authentication tokens manually.

Always rely on Firebase Authentication for user authentication.

---

# Scalability Guidelines

The project should be designed so future features can be added without major structural changes.

Future additions include:

- ESP32 integration
- Live sensor monitoring
- Automatic ventilation
- Push notifications
- Reports
- Analytics
- Multiple restaurants
- Multiple IoT devices

Current architecture should already support these future enhancements.

---

# Testing Guidelines

Phase 1 should verify:

Authentication

- User registration
- User login
- Forgot password
- Persistent login
- Logout

Firestore

- User profile creation
- User role storage
- User profile retrieval

Navigation

- Splash Screen routing
- Login navigation
- Dashboard navigation
- Logout navigation

Role-Based Access

Manager:

- Full navigation visibility

Staff:

- Restricted navigation
- Hidden manager-only features

Dashboard

- Displays mock data correctly
- Loads user information from Firestore
- UI remains responsive

---

# Definition of Done

Phase 1 is complete when all of the following requirements are met:

Authentication

- Splash Screen is functional.
- Login is connected to Firebase Authentication.
- Sign Up successfully creates Firebase Authentication accounts.
- Forgot Password works.
- Logout works correctly.
- Authentication persists across app launches.

Cloud Firestore

- User profile is created.
- Full Name is stored.
- Restaurant Name is stored.
- Email is stored.
- Role is stored.
- Created timestamp is stored.

User Roles

- Manager role functions correctly.
- Staff role functions correctly.
- Role is retrieved after login.
- Manager-only features are hidden from Staff.
- A single shared Dashboard is used for both roles.

Dashboard

- Displays user information from Firestore.
- Displays realistic placeholder values.
- Navigation works correctly.
- Placeholder pages exist for future features.

Architecture

- MVVM Architecture is followed.
- Repository Pattern is followed.
- UI is separated from business logic.
- Firebase logic exists only in repositories.
- ViewModels expose StateFlow.
- Code is modular and maintainable.

Quality

- Project builds successfully.
- No critical runtime errors.
- Material Design 3 is used consistently.
- Light Mode and Dark Mode are supported.
- Codebase is clean and ready for Phase 2.

---

# Final AI Instructions

This document serves as the primary development specification for the ScentGuard Vent project.

During Phase 1, the AI should focus exclusively on delivering a production-ready Android application using Kotlin, Jetpack Compose, Firebase Authentication, and Cloud Firestore.

The AI must not implement ESP32 communication, sensor integration, Firebase Realtime Database, automatic ventilation, reports, analytics, or any other IoT-related functionality until Phase 2.

All Phase 1 code should be clean, modular, scalable, and designed so that future IoT features can be integrated with minimal refactoring while preserving the existing architecture and user interface.
