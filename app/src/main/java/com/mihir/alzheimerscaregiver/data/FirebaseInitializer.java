package com.mihir.alzheimerscaregiver.data;

import android.content.Context;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Centralized Firebase initialization class that ensures proper initialization order
 * and prevents the "FirebaseFirestore has already been started" error.
 */
public class FirebaseInitializer {
    
    private static final String TAG = "FirebaseInitializer";
    private static boolean isInitialized = false;
    private static FirebaseFirestore firestoreInstance = null;
    private static FirebaseAuth authInstance = null;
    
    /**
     * Initialize Firebase components. This should be called early in the app lifecycle,
     * preferably in Application.onCreate() or MainActivity.onCreate().
     */
    public static synchronized void initialize(Context context) {
        if (isInitialized) {
            Log.d(TAG, "Firebase already initialized");
            return;
        }
        
        try {
            Log.d(TAG, "Initializing Firebase...");
            
            // Initialize Firebase App if not already done
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
                Log.d(TAG, "Firebase App initialized");
            }
            
            // Initialize Firestore with settings
            firestoreInstance = FirebaseFirestore.getInstance();
            if (firestoreInstance == null) {
                throw new RuntimeException("FirebaseFirestore.getInstance() returned null");
            }
            
            // Apply Firestore settings
            try {
                FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build();
                
                firestoreInstance.setFirestoreSettings(settings);
                Log.d(TAG, "Firestore settings applied successfully");
            } catch (IllegalStateException e) {
                Log.w(TAG, "Firestore settings already applied or instance already started", e);
                // Don't fail if settings can't be applied - the instance is still usable
            }
            
            // Initialize Auth
            authInstance = FirebaseAuth.getInstance();
            if (authInstance == null) {
                throw new RuntimeException("FirebaseAuth.getInstance() returned null");
            }
            
            isInitialized = true;
            Log.d(TAG, "Firebase initialization completed successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the initialized Firestore instance
     */
    public static synchronized FirebaseFirestore getFirestore() {
        if (!isInitialized || firestoreInstance == null) {
            throw new IllegalStateException("Firebase not initialized. Call FirebaseInitializer.initialize() first.");
        }
        return firestoreInstance;
    }
    
    /**
     * Get the initialized Auth instance
     */
    public static synchronized FirebaseAuth getAuth() {
        if (!isInitialized || authInstance == null) {
            throw new IllegalStateException("Firebase not initialized. Call FirebaseInitializer.initialize() first.");
        }
        return authInstance;
    }
    
    /**
     * Check if Firebase is initialized
     */
    public static synchronized boolean isInitialized() {
        return isInitialized;
    }
}
