package com.mihir.alzheimerscaregiver.caretaker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.caretaker.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch mmseSwitch;
    private SharedPreferences prefs;
    private String linkedPatientId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker_settings);

        prefs = getSharedPreferences("CaretakerApp", MODE_PRIVATE);
        linkedPatientId = prefs.getString("linkedPatientId", null);

        mmseSwitch = findViewById(R.id.switchMmseMonthly);

        mmseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (linkedPatientId != null) {
                    FirebaseFirestore.getInstance()
                            .collection("patients")
                            .document(linkedPatientId)
                            .collection("settings")
                            .document("reminders")
                            .set(java.util.Collections.singletonMap("mmseMonthlyEnabled", isChecked));
                }
            }
        });
    }
}


