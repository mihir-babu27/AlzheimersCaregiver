package com.mihir.alzheimerscaregiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import com.mihir.alzheimerscaregiver.auth.FirebaseAuthManager;
import com.mihir.alzheimerscaregiver.face_recognition.FaceRecognitionActivity;


public class MainActivity extends AppCompatActivity {

    // UI Elements
    private TextView welcomeText;
    private TextView nameText;
    private CardView medicationCard, tasksCard, memoryCard, photosCard, emergencyCard, mmseCard, objectDetectionCard;
    
    // Firebase Auth Manager
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase early to prevent initialization issues
        try {
            com.mihir.alzheimerscaregiver.data.FirebaseInitializer.initialize(this);
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error initializing Firebase", e);
            // Continue with app initialization even if Firebase fails
        }

        // Initialize notification channels early
        com.mihir.alzheimerscaregiver.notifications.NotificationUtils.ensureChannels(this);
        
        // Request notification permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.activity.result.ActivityResultLauncher<String> launcher = registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            android.util.Log.d("MainActivity", "Notification permission granted");
                        } else {
                            android.util.Log.w("MainActivity", "Notification permission denied");
                        }
                    }
                );
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Initialize Firebase Auth Manager
        authManager = new FirebaseAuthManager();

        // Check if user is signed in
        if (!authManager.isPatientSignedIn()) {
            navigateToAuth();
            return;
        }

        // Initialize UI elements
        initializeViews();

        // Set up dynamic welcome message
        setupWelcomeMessage();

        // Set up click listeners for all cards
        setupClickListeners();

        // Schedule MMSE test notifications for patient
        try {
            String patientId = authManager.getCurrentPatientId();
            if (patientId != null) {
                com.mihir.alzheimerscaregiver.mmse.MmseScheduleManager.scheduleAll(this, patientId);
            }
        } catch (Exception ignore) {}

        // Sync MMSE monthly reminder from settings if available
        try {
            syncMmseReminder();
        } catch (Exception ignore) {}
    }

    private void syncMmseReminder() {
        String patientId = authManager.getCurrentPatientId();
        if (patientId == null) return;
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("patients").document(patientId)
                .collection("settings").document("reminders")
                .get()
                .addOnSuccessListener(doc -> {
                    boolean enabled = doc != null && doc.exists() && Boolean.TRUE.equals(doc.getBoolean("mmseMonthlyEnabled"));
                    if (enabled) {
                        com.mihir.alzheimerscaregiver.notifications.MmseReminderScheduler.scheduleMonthly(this);
                    } else {
                        com.mihir.alzheimerscaregiver.notifications.MmseReminderScheduler.cancel(this);
                    }
                });
    }

    /**
     * Initialize all UI elements
     */
    private void initializeViews() {
        // Text views
        welcomeText = findViewById(R.id.welcomeText);
        nameText = findViewById(R.id.nameText);

        // Feature cards
        medicationCard = findViewById(R.id.medicationCard);
        tasksCard = findViewById(R.id.tasksCard);
        memoryCard = findViewById(R.id.memoryCard);
        photosCard = findViewById(R.id.photosCard);
        mmseCard = findViewById(R.id.mmseCard);
        emergencyCard = findViewById(R.id.emergencyCard);
        objectDetectionCard = findViewById(R.id.objectDetectionCard);
    }

    /**
     * Set up dynamic welcome message based on time of day
     */
    private void setupWelcomeMessage() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour < 12) {
            greeting = "Good Morning";
        } else if (hour < 17) {
            greeting = "Good Afternoon";
        } else {
            greeting = "Good Evening";
        }

        welcomeText.setText(greeting);

        // Get patient name from Firebase Auth
        String patientId = authManager.getCurrentPatientId();
        if (patientId != null) {
            authManager.getPatientData(patientId, new FirebaseAuthManager.PatientDataCallback() {
                @Override
                public void onSuccess(com.mihir.alzheimerscaregiver.data.entity.PatientEntity patient) {
                    if (patient != null && patient.name != null) {
                        nameText.setText(patient.name);
                    } else {
                        nameText.setText("Friend");
                    }
                }
                
                @Override
                public void onError(String error) {
                    nameText.setText("Friend");
                }
            });
        } else {
            nameText.setText("Friend");
        }
    }

    /**
     * Set up click listeners for all feature cards
     */
    private void setupClickListeners() {

        // Medication Card
        medicationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add haptic feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                showToast("Opening Medication Reminders...");
                Intent intent = new Intent(MainActivity.this, RemindersActivity.class);
                intent.putExtra(RemindersActivity.EXTRA_MEDICATION_MODE, true);
                startActivity(intent);
            }
        });

        // Daily Tasks Card
        tasksCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add haptic feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                // Create intent to start TasksActivity
                Intent intent = new Intent(MainActivity.this, TasksActivity.class);
                startActivity(intent);
            }
        });

        // Memory Games Card
        memoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                // Navigate to GameSelectionActivity
                Intent intent = new Intent(MainActivity.this, GameSelectionActivity.class);
                startActivity(intent);
            }
        });

        // Face Recognition Card
        photosCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add haptic feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                // Open Face Recognition
                Intent intent = new Intent(MainActivity.this, FaceRecognitionActivity.class);
                startActivity(intent);

            }
        });
        // Emergency Card
        emergencyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                // Create intent to start EmergencyActivity
                Intent intent = new Intent(MainActivity.this, EmergencyActivity.class);
                startActivity(intent);
            }
        });

        // MMSE Quiz Card
        mmseCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                Intent intent = new Intent(MainActivity.this, MmseQuizActivity.class);
                startActivity(intent);
            }
        });

        // Object Detection Card
        objectDetectionCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                showToast("Opening Object Detection...");
                Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Helper method to show toast messages
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to update the tasks count (you can call this from other activities)
     */
    public void updateTasksRemaining(int count) {
        // You can add logic here to update the "3 tasks remaining" text
        // This would typically be connected to a database or shared preferences
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update welcome and name when returning to the app
        setupWelcomeMessage();

        // You could also refresh task counts, medication reminders, etc.
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            // Navigate to Patient Profile
            Intent intent = new Intent(this, PatientProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_sign_out) {
            // Sign out user
            authManager.signOut();
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            navigateToAuth();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void navigateToAuth() {
        Intent intent = new Intent(this, com.mihir.alzheimerscaregiver.auth.AuthenticationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}