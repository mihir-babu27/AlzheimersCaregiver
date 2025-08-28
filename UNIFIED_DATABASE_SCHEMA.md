# Unified Database Schema Documentation

## Overview
This document describes the unified database structure between the **AlzheimersCaregiver** (Patient App) and **CaretakerApp** (Caretaker App). Both apps now use the same entity classes and collection names to ensure data consistency.

## Database Structure

### Collections
All data is stored under the `patients/{patientId}` document with the following subcollections:

1. **`reminders`** - For medication reminders and general reminders
2. **`tasks`** - For daily tasks and activities  
3. **`contacts`** - For emergency contacts and family members

### Entity Classes

#### 1. ReminderEntity (Used for Medications)
```java
public class ReminderEntity {
    public String id;                    // Firebase document ID
    public String title;                 // Medication name + dosage (e.g., "Donepezil - 10mg")
    public String description;           // Time instruction (e.g., "Take at 08:00 AM")
    public Long scheduledTimeEpochMillis; // When to take the medication
    public boolean isCompleted;          // Whether the reminder is completed
}
```

**Storage Location**: `patients/{patientId}/reminders/{reminderId}`

**Mapping from CaretakerApp**:
- **Medication Name + Dosage** → `title` field
- **Time** → `description` field  
- **Scheduled Date + Time** → `scheduledTimeEpochMillis` field

#### 2. TaskEntity
```java
public class TaskEntity {
    public String id;                    // Firebase document ID
    public String name;                  // Task name (e.g., "Go for a walk")
    public String description;           // Task description (e.g., "30 minutes in the garden")
    public boolean isCompleted;          // Whether the task is completed
    public String category;              // Task category (e.g., "General", "Exercise")
    public Long scheduledTimeEpochMillis; // When the task is due
    public boolean isRecurring;          // Whether the task repeats
    public String recurrenceRule;        // Recurrence pattern (e.g., "DAILY", "WEEKLY:MON,WED,FRI")
}
```

**Storage Location**: `patients/{patientId}/tasks/{taskId}`

**Mapping from CaretakerApp**:
- **Task Name** → `name` field
- **Task Description** → `description` field
- **Due Date + Time** → `scheduledTimeEpochMillis` field
- **Category** → Set to "General" by default

#### 3. ContactEntity
```java
public class ContactEntity {
    public String id;                    // Firebase document ID
    public String name;                  // Contact name (e.g., "Dr. Sharma")
    public String phoneNumber;           // Phone number (e.g., "+91-9876543210")
    public String relationship;          // Relationship (e.g., "Primary Doctor")
    public boolean isPrimary;            // Whether this is the primary contact
}
```

**Storage Location**: `patients/{patientId}/contacts/{contactId}`

**Mapping from CaretakerApp**:
- **Contact Name** → `name` field
- **Phone Number** → `phoneNumber` field
- **Relation** → `relationship` field
- **Primary Status** → Set to `false` by default

## Data Flow

### Caretaker App → Patient App
1. **Caretaker** creates medication reminder using `AddMedicationActivity`
2. **Data** is stored as `ReminderEntity` in `patients/{patientId}/reminders`
3. **Patient App** reads from the same collection and displays in `RemindersActivity`
4. **Patient** can mark reminders as completed

### Caretaker App → Patient App
1. **Caretaker** creates task using `AddTaskActivity`
2. **Data** is stored as `TaskEntity` in `patients/{patientId}/tasks`
3. **Patient App** reads from the same collection and displays in `TasksActivity`
4. **Patient** can mark tasks as completed

### Caretaker App → Patient App
1. **Caretaker** adds emergency contact using `AddEmergencyContactActivity`
2. **Data** is stored as `ContactEntity` in `patients/{patientId}/contacts`
3. **Patient App** reads from the same collection and displays in emergency contacts section

## Key Changes Made

### 1. Entity Unification
- **Removed** custom `MedicationEntity` from CaretakerApp
- **Used** `ReminderEntity` for medications (stored in `reminders` collection)
- **Ensured** both apps use identical entity classes

### 2. Collection Names
- **Medications**: Now stored in `reminders` collection (not `medications`)
- **Tasks**: Stored in `tasks` collection (same as before)
- **Contacts**: Stored in `contacts` collection (not `emergencyContacts`)

### 3. Field Mapping
- **Medication**: `name + dosage` → `title`, `time` → `description`
- **Tasks**: Direct field mapping with proper scheduling
- **Contacts**: Direct field mapping

### 4. Time Scheduling
- **Added** proper date and time pickers in CaretakerApp
- **Fixed** issue where creation time was used instead of scheduled time
- **Implemented** proper timestamp parsing for scheduled dates/times

## Security Rules

The Firestore security rules ensure:
- **Patients** can only access their own data
- **Caretakers** can only access data for patients they're linked to
- **Linking** is managed through `caretakerPatients/{caretakerUid}/linkedPatients/{patientId}`

## Benefits of Unified Schema

1. **Data Consistency**: Both apps read/write to the same collections
2. **Real-time Sync**: Changes in CaretakerApp immediately appear in PatientApp
3. **Maintainability**: Single source of truth for data structure
4. **User Experience**: Seamless data flow between caretaker and patient

## Future Enhancements

1. **Recurring Medications**: Add support for daily/weekly medication schedules
2. **Task Categories**: Allow caretakers to assign specific categories to tasks
3. **Contact Priority**: Implement contact priority system for emergency situations
4. **Notification System**: Add push notifications for reminders and tasks
