
package com.mihir.alzheimerscaregiver.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.data.FirebaseConfig;

import java.util.ArrayList;
import java.util.List;

public class ContactRepository {

    private final FirebaseFirestore db = FirebaseConfig.getInstance();
    private final CollectionReference contactsRef = db.collection("contacts");

    public LiveData<List<ContactEntity>> getAll() {
        MutableLiveData<List<ContactEntity>> liveData = new MutableLiveData<>();
        contactsRef.orderBy("name")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<ContactEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            ContactEntity entity = dc.getDocument().toObject(ContactEntity.class);
                            if (entity != null) {
                                entity.id = dc.getDocument().getId();
                                list.add(entity);
                            }
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    public LiveData<List<ContactEntity>> search(String query) {
        MutableLiveData<List<ContactEntity>> liveData = new MutableLiveData<>();
        contactsRef.whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        liveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<ContactEntity> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            ContactEntity entity = dc.getDocument().toObject(ContactEntity.class);
                            if (entity != null) {
                                entity.id = dc.getDocument().getId();
                                list.add(entity);
                            }
                        }
                    }
                    liveData.setValue(list);
                });
        return liveData;
    }

    public void insert(ContactEntity contact) {
        if (contact.id == null || contact.id.isEmpty()) {
            DocumentReference docRef = contactsRef.document();
            contact.id = docRef.getId();
            docRef.set(contact)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        } else {
            contactsRef.document(contact.id).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void update(ContactEntity contact) {
        if (contact.id != null && !contact.id.isEmpty()) {
            contactsRef.document(contact.id).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void delete(ContactEntity contact) {
        if (contact.id != null && !contact.id.isEmpty()) {
            contactsRef.document(contact.id).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Success
                    })
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
        }
    }

    public void setPrimary(String id) {
        contactsRef.document(id).update("isPrimary", true)
                .addOnSuccessListener(aVoid -> {
                    // Success
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}


