# CaretakerApp

A comprehensive Android application for Alzheimer's caregivers to manage patient medications, tasks, and emergency contacts. Built with Java, Gradle, and Firebase.

## 🚀 Features

### Authentication
- **Firebase Authentication** with email/password
- User registration and login
- Secure session management

### Patient Management
- **Patient Linking**: Caretakers can link their account to a patient using a unique Patient ID
- **Secure Access**: Only linked caretakers can access patient data

### Core Functionality
- **Medication Management**: Add medication reminders with name, dosage, and time
- **Task Management**: Create task reminders with descriptions and due times
- **Emergency Contacts**: Manage emergency contact information for patients

## 🏗️ Technical Architecture

### Technology Stack
- **Language**: Java 17
- **Build System**: Gradle 8.5
- **Minimum SDK**: Android API 26 (Android 8.0)
- **Target SDK**: Android API 34 (Android 14)
- **UI Framework**: Material Design Components

### Firebase Integration
- **Authentication**: Email/password authentication
- **Firestore**: NoSQL database for data storage
- **Security Rules**: Comprehensive access control

### Project Structure
```
CaretakerApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/mihir/alzheimerscaregiver/caretaker/
│   │   │   ├── auth/
│   │   │   │   ├── LoginActivity.java
│   │   │   │   └── RegisterActivity.java
│   │   │   ├── MainActivity.java
│   │   │   ├── PatientLinkActivity.java
│   │   │   ├── AddMedicationActivity.java
│   │   │   ├── AddTaskActivity.java
│   │   │   └── AddEmergencyContactActivity.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── values/
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json
├── build.gradle
├── gradle.properties
├── firestore.rules
└── README.md
```

## 🔐 Firebase Security Rules

The app implements comprehensive Firestore security rules:

- **Caretakers**: Can only modify patient data if linked via `caretakerPatients/{caretakerUid}/linkedPatients/{patientId}`
- **Patients**: Can only access their own data under `patients/{patientId}`
- **Secure Access**: All operations require authentication and proper authorization

## 📱 User Interface

### Screens
1. **Login/Register**: Firebase authentication
2. **Patient Link**: Enter Patient ID to link account
3. **Main Dashboard**: Access to all features
4. **Add Medication**: Form for medication reminders
5. **Add Task**: Form for task reminders
6. **Add Emergency Contact**: Form for contact information

### Design Principles
- **Material Design**: Modern, intuitive interface
- **Responsive Layout**: Works on various screen sizes
- **Accessibility**: Clear labels and user feedback

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Java 17 or later
- Firebase project with Authentication and Firestore enabled

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on device/emulator

### Firebase Setup
1. Create a Firebase project
2. Enable Authentication (Email/Password)
3. Enable Firestore Database
4. Download `google-services.json` and place in `app/` directory
5. Deploy Firestore security rules from `firestore.rules`

## 🔧 Build Configuration

### Gradle Configuration
- **Android Gradle Plugin**: 8.2.2
- **Gradle Version**: 8.5
- **Java Compatibility**: 17

### Dependencies
- **AndroidX**: Core libraries for modern Android development
- **Material Design**: UI components and theming
- **Firebase BOM**: Dependency management for Firebase services

## 📊 Data Structure

### Firestore Collections
```
caretakerPatients/{caretakerUid}/linkedPatients/{patientId}
├── linkedAt: timestamp

patients/{patientId}/
├── medications/{medicationId}
│   ├── name: string
│   ├── dosage: string
│   ├── time: string
│   └── createdBy: string (caretakerUid)
├── tasks/{taskId}
│   ├── task: string
│   ├── description: string
│   ├── dueTime: timestamp
│   └── createdBy: string (caretakerUid)
└── emergencyContacts/{contactId}
    ├── name: string
    ├── relation: string
    ├── phone: string
    └── createdBy: string (caretakerUid)
```

## ✅ Build Status

- **✅ Gradle Build**: Successful
- **✅ Debug APK**: Generated successfully
- **✅ Firebase Integration**: Configured and working
- **✅ Package Structure**: Refactored and organized
- **✅ Dependencies**: All resolved

## 🎯 Next Steps

### Immediate Enhancements
- Implement "Forgot Password" functionality
- Add patient data viewing capabilities
- Implement data editing and deletion

### Future Features
- Push notifications for reminders
- Offline data synchronization
- Multi-patient support
- Data export functionality
- Advanced reminder scheduling

## 🤝 Contributing

This project is part of the Alzheimer's Caregiver ecosystem. For contributions or questions, please refer to the main project documentation.

## 📄 License

This project is proprietary and confidential. All rights reserved.

---

**Built with ❤️ for Alzheimer's caregivers and patients**
