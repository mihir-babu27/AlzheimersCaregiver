package com.mihir.alzheimerscaregiver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ObjectDetectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detection);

        Button startDetectionButton = findViewById(R.id.startDetectionButton);
        startDetectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Launch the YOLOv9 MainActivity
                    Intent intent = new Intent(ObjectDetectionActivity.this, com.mihir.alzheimerscaregiver.objectdetection.MainActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    // Handle any launch errors gracefully
                    Toast.makeText(ObjectDetectionActivity.this, 
                        "Error starting object detection: " + e.getMessage(), 
                        Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
