package com.mihir.alzheimerscaregiver.data;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirebaseConfig {
    
    private static FirebaseFirestore db;
    
    public static FirebaseFirestore getInstance() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
            
            // Enable offline persistence
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build();
            
            db.setFirestoreSettings(settings);
        }
        return db;
    }
}
