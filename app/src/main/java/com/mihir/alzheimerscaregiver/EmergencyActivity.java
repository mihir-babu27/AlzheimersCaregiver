package com.mihir.alzheimerscaregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihir.alzheimerscaregiver.data.entity.ContactEntity;
import com.mihir.alzheimerscaregiver.ui.contacts.ContactsEntityAdapter;
import com.mihir.alzheimerscaregiver.ui.viewmodel.ContactViewModel;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class EmergencyActivity extends AppCompatActivity implements ContactsEntityAdapter.OnContactEntityInteractionListener {

    // UI Elements
    private ImageButton backButton;
    private RecyclerView emergencyContactsRecyclerView;
    private FloatingActionButton addContactFab;

    // Room-backed adapter and VM
    private ContactsEntityAdapter contactsAdapter;
    private ContactViewModel contactViewModel;

    // Permission request code
    private static final int CALL_PHONE_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Initialize UI elements
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Initialize RecyclerView, adapter and ViewModel
        setupRecyclerView();

        // Check and request phone permission
        checkPhonePermission();
    }

    /**
     * Initialize all UI elements
     */
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        emergencyContactsRecyclerView = findViewById(R.id.emergencyContactsRecyclerView);
        addContactFab = findViewById(R.id.addContactFab);
    }

    /**
     * Set up click listeners for interactive elements
     */
    private void setupClickListeners() {
        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Provide haptic feedback
                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                // Go back to main activity
                finish();
            }
        });

        addContactFab.setOnClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            showAddOrEditContactDialog(null);
        });
    }

    /**
     * Create sample emergency contacts
     */
    private void createSampleContacts() {}

    /**
     * Set up RecyclerView with LinearLayoutManager and ContactsAdapter
     */
    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        emergencyContactsRecyclerView.setLayoutManager(layoutManager);

        contactsAdapter = new ContactsEntityAdapter(this);
        contactsAdapter.setListener(this);
        emergencyContactsRecyclerView.setAdapter(contactsAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                onItemSwipedToDelete(contactsAdapter.getItem(position), position);
            }
        });
        helper.attachToRecyclerView(emergencyContactsRecyclerView);

        contactViewModel = new ViewModelProvider(this).get(ContactViewModel.class);
        contactViewModel.getContacts().observe(this, contacts -> contactsAdapter.submitList(contacts));
    }

    /**
     * Check if CALL_PHONE permission is granted, request if not
     */
    private void checkPhonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_PERMISSION_REQUEST);
        }
    }

    /**
     * Handle permission request result
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PHONE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                showToast("Phone permission granted. You can now make calls directly.");
            } else {
                // Permission denied
                showToast("Phone permission denied. Calls will open the dialer instead.");
            }
        }
    }

    /**
     * Add a new contact to the emergency contacts list
     * @param contact The contact to add
     */
    public void addEmergencyContact(Contact contact) {
        // Legacy method not used; kept for compatibility
    }

    /**
     * Remove a contact from the emergency contacts list
     * @param position The position of the contact to remove
     */
    public void removeEmergencyContact(int position) {
        // Legacy method not used; kept for compatibility
    }

    /**
     * Refresh the contacts list (useful when returning from other activities)
     */
    public void refreshContactsList() {
        // VM + LiveData keep this updated automatically
    }

    /**
     * Helper method to show toast messages
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // LiveData handles list updates
    }

    // Adapter callbacks
    @Override
    public void onItemClicked(ContactEntity contact) {
        showAddOrEditContactDialog(contact);
    }

    @Override
    public void onItemSwipedToDelete(ContactEntity contact, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (d, w) -> contactViewModel.delete(contact))
                .setNegativeButton("Cancel", (d, w) -> contactsAdapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> contactsAdapter.notifyItemChanged(position))
                .show();
    }

    @Override
    public void onSetPrimary(ContactEntity contact) {
        contactViewModel.setPrimary(contact.id);
    }

    private void showAddOrEditContactDialog(ContactEntity existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_contact, null, false);
        EditText inputName = view.findViewById(R.id.inputContactName);
        EditText inputPhone = view.findViewById(R.id.inputPhoneNumber);
        EditText inputRelationship = view.findViewById(R.id.inputRelationship);
        CheckBox checkPrimary = view.findViewById(R.id.checkPrimary);

        if (existing != null) {
            inputName.setText(existing.name);
            inputPhone.setText(existing.phoneNumber);
            inputRelationship.setText(existing.relationship);
            checkPrimary.setChecked(existing.isPrimary);
        }

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add contact" : "Edit contact")
                .setView(view)
                .setPositiveButton(existing == null ? "Add" : "Save", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String phone = inputPhone.getText().toString().trim();
                    String relationship = inputRelationship.getText().toString().trim();
                    boolean isPrimary = checkPrimary.isChecked();

                    if (TextUtils.isEmpty(name)) { showToast("Name required"); return; }
                    if (!isValidPhone(phone)) { showToast("Enter valid phone number"); return; }

                    if (existing == null) {
                        ContactEntity entity = new ContactEntity(name, phone, emptyToNull(relationship), isPrimary);
                        contactViewModel.insert(entity);
                    } else {
                        existing.name = name;
                        existing.phoneNumber = phone;
                        existing.relationship = emptyToNull(relationship);
                        existing.isPrimary = isPrimary;
                contactViewModel.update(existing);
                    }
                })
                .setNegativeButton("Cancel", null)
                //.setNeutralButton("Export CSV", (d, w) -> exportContactsCsv())

                .show();
    }

    private boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) return false;
        String digits = phone.replaceAll("[^0-9]", "");
        return digits.length() == 10;
    }

    private void exportContactsCsv() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir != null && !dir.exists()) dir.mkdirs();
            File file = new File(dir, "emergency_contacts.csv");

            StringBuilder csv = new StringBuilder();
            csv.append("Name,Phone,Relationship,Primary\n");
            for (int i = 0; i < contactsAdapter.getItemCount(); i++) {
                ContactEntity c = contactsAdapter.getItem(i);
                csv.append(escapeCsv(c.name)).append(",")
                        .append(escapeCsv(c.phoneNumber)).append(",")
                        .append(escapeCsv(c.relationship == null ? "" : c.relationship)).append(",")
                        .append(c.isPrimary ? "Yes" : "No")
                        .append("\n");
            }

            try (FileWriter writer = new FileWriter(file, false)) {
                writer.write(csv.toString());
            }

            Toast.makeText(this, "Exported to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to export CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\n") || out.contains("\r")) {
            return "\"" + out + "\"";
        }return out;
    }

    private String emptyToNull(String s) { return TextUtils.isEmpty(s) ? null : s; }
}