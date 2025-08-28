package com.mihir.alzheimerscaregiver;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MmseResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmse_result);

        int score = getIntent().getIntExtra("score", 0);
        String interpretation = getIntent().getStringExtra("interpretation");

        TextView scoreText = findViewById(R.id.scoreText);
        TextView interpretationText = findViewById(R.id.interpretationText);

        scoreText.setText(String.valueOf(score));
        interpretationText.setText(interpretation != null ? interpretation : "");

        TextView feedbackText = findViewById(R.id.feedbackText);
        String feedback = getIntent().getStringExtra("feedback");
        if (feedbackText != null && feedback != null) {
            feedbackText.setText(feedback);
        }
    }
}


