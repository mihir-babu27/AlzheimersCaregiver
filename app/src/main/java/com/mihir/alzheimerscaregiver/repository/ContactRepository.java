
package com.mihir.alzheimerscaregiver.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

import java.util.ArrayList;
import java.util.List;

public class ContactRepository {

    private final FirebaseFirestore db = FirebaseConfig.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private CollectionReference getContactsRef() {
        String patientId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "default";
        return db.collection("patients").document(patientId).collection("contacts");
    }

    public interface FirebaseCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void getAll(FirebaseCallback<List<ContactEntity>> callback) {
        getContactsRef().orderBy("name")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContactEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            ContactEntity entity = doc.toObject(ContactEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void search(String query, FirebaseCallback<List<ContactEntity>> callback) {
        getContactsRef().whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ContactEntity> list = new ArrayList<>();
                    if (queryDocumentSnapshots != null) {
                        for (var doc : queryDocumentSnapshots) {
                            ContactEntity entity = doc.toObject(ContactEntity.class);
                            if (entity != null) {
                                entity.id = doc.getId();
                                list.add(entity);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    public void insert(ContactEntity contact, FirebaseCallback<Void> callback) {
        if (contact.id == null || contact.id.isEmpty()) {
            DocumentReference docRef = getContactsRef().document();
            contact.id = docRef.getId();
            docRef.set(contact)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        } else {
            getContactsRef().document(contact.id).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        }
    }

    public void update(ContactEntity contact, FirebaseCallback<Void> callback) {
        if (contact.id != null && !contact.id.isEmpty()) {
            getContactsRef().document(contact.id).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        } else {
            callback.onError("Contact ID is required for update");
        }
    }

    public void delete(ContactEntity contact, FirebaseCallback<Void> callback) {
        if (contact.id != null && !contact.id.isEmpty()) {
            getContactsRef().document(contact.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
        } else {
            callback.onError("Contact ID is required for deletion");
        }
    }

    public void setPrimary(String id, FirebaseCallback<Void> callback) {
        getContactsRef().document(id).update("isPrimary", true)
                .addOnSuccessListener(aVoid -> {
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
}


