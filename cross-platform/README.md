# Client Management System (ClientFlow) - Cross-Platform Architecture

This repository contains the enterprise **Client Management System** providing cross-platform synchronization across **Native Android (Kotlin & Jetpack Compose)**, **iOS & Mobile (React Native + Expo)**, and **Web (React + TypeScript)**.

---

## 1. System Overview

### Shared Data & RBAC Roles
* **Manager**: Executive overview, client/team/project administration, 2-day deadline alerts, immutable compliance audit trail.
* **Team Lead**: Team deliverable supervision, task assignment, 5-day deadline alerts, proof verification review queue.
* **Team Member**: Personal assigned deliverables, status updates, completion proof uploads (camera photo, screenshot, documents), feedback tracking.

### Core Modules
* **Per-Task Audit Trail**: Comprehensive change history recorded within each task details modal (who modified, timestamp, old vs new values).
* **Advanced Search & Filtering**: Multi-criteria search by keyword, status, date ranges (Overdue, ≤ 2 Days, ≤ 5 Days, This Week), and assigned user.
* **Real-time Notifications**: Real-time push/in-app notices for new task assignments, approaching deadlines (Manager: 2 days, Lead: 5 days), and review approvals/rejections.

---

## 2. Running the Cross-Platform Mobile & Web App (Expo)

The cross-platform Expo application is located in `/cross-platform/`.

### Prerequisites
* Node.js 18+ and npm
* Expo CLI (`npm install -g expo-cli`)
* Expo Go app on physical iOS/Android device (or local Xcode / Android Studio simulator)

### Step 1: Install Dependencies
```bash
cd cross-platform
npm install
```

### Step 2: Configure Firebase Environment
Create a `.env` file in `/cross-platform/` with your Firebase credentials:
```env
EXPO_PUBLIC_FIREBASE_API_KEY=your_api_key
EXPO_PUBLIC_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
EXPO_PUBLIC_FIREBASE_PROJECT_ID=your_project_id
EXPO_PUBLIC_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
EXPO_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=your_sender_id
EXPO_PUBLIC_FIREBASE_APP_ID=your_app_id
```

### Step 3: Run on Web
```bash
npx expo start --web
```
This boots the React TypeScript web application at `http://localhost:8081`.

### Step 4: Run on Mobile (Android & iOS)
* **Start Metro Bundler:**
  ```bash
  npx expo start
  ```
* **iOS:** Press `i` in the terminal to open in iOS Simulator, or scan the QR code with the Camera app using Expo Go on an iPhone.
* **Android:** Press `a` in the terminal to launch the Android emulator, or scan the QR code with Expo Go on an Android phone.

---

## 3. Deploying Cloud Firestore Security Rules

To enforce zero-trust role-based access control, deploy the included `/cross-platform/firestore.rules` using Firebase CLI:

```bash
firebase deploy --only firestore:rules
```

---

## 4. Native Android Application (AI Studio Emulator)

The native Android app in the project root (`/app`) is compiled with Kotlin and Jetpack Compose. You can run, test, and preview it directly in the Google AI Studio Streaming Emulator.
