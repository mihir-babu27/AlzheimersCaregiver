package com.mihir.alzheimerscaregiver.caretaker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.caretaker.R;

import java.util.Map;

public class MmseResultDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caretaker_mmse_result_detail);

        String resultId = getIntent().getStringExtra("resultId");
        String patientId = getIntent().getStringExtra(MmseResultsActivity.EXTRA_PATIENT_ID);

        TextView title = findViewById(R.id.titleText);
        TextView sectionsText = findViewById(R.id.sectionsText);

        if (patientId != null && resultId != null) {
            FirebaseFirestore.getInstance()
                .collection("patients").document(patientId)
                .collection("mmse_results").document(resultId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        Map<String, Object> map = doc.getData();
                        if (map != null) {
                            Object dateTaken = map.get("dateTaken");
                            Object totalScore = map.get("totalScore");
                            Object interpretation = map.get("interpretation");
                            title.setText("Score: " + totalScore + "  (" + interpretation + ")");

                            Object sectionScores = map.get("sectionScores");
                            if (sectionScores instanceof Map) {
                                StringBuilder sb = new StringBuilder();
                                for (Object k : ((Map<?, ?>) sectionScores).keySet()) {
                                    Object v = ((Map<?, ?>) sectionScores).get(k);
                                    sb.append(String.valueOf(k)).append(": ").append(String.valueOf(v)).append("\n");
                                }
                                sectionsText.setText(sb.toString());
                            }
                        }
                    }
                });
        }
    }
}


