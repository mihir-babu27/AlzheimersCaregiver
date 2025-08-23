package com.mihir.alzheimerscaregiver.data;

import android.content.Context;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Legacy Firebase configuration class - now delegates to FirebaseInitializer
 * @deprecated Use FirebaseInitializer.initialize() and FirebaseInitializer.getFirestore() instead
 */
@Deprecated
public class FirebaseConfig {
    
    private static final String TAG = "FirebaseConfig";
    private static FirebaseFirestore db;
    
    public static FirebaseFirestore getInstance() {
        if (db == null) {
            try {
                // Try to use the new initializer if available
                if (FirebaseInitializer.isInitialized()) {
                    db = FirebaseInitializer.getFirestore();
                    Log.d(TAG, "Using FirebaseInitializer instance");
                } else {
                    // Fallback to direct initialization (for backward compatibility)
                    Log.w(TAG, "FirebaseInitializer not available, using direct initialization");
                    db = FirebaseFirestore.getInstance();
                    
                    if (db == null) {
                        throw new RuntimeException("FirebaseFirestore.getInstance() returned null");
                    }
                }
                
                Log.d(TAG, "Firebase Firestore instance retrieved successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error getting Firebase Firestore instance", e);
                throw new RuntimeException("Failed to get Firebase Firestore instance: " + e.getMessage(), e);
            }
        }
        return db;
    }
    
    /**
     * Initialize Firebase with context (recommended approach)
     */
    public static void initialize(Context context) {
        FirebaseInitializer.initialize(context);
        // Update our cached instance
        db = FirebaseInitializer.getFirestore();
    }
}
