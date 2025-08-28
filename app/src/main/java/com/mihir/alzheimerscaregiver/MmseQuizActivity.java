package com.mihir.alzheimerscaregiver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.mihir.alzheimerscaregiver.data.entity.MmseResult;
import com.mihir.alzheimerscaregiver.repository.MmseResultRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MmseQuizActivity extends AppCompatActivity {

    private TextView questionTitle;
    private View textInputContainer;
    private EditText textInput;
    private View multipleChoiceContainer;
    private RadioGroup radioGroup;
    private View drawingContainer;
    private com.mihir.alzheimerscaregiver.views.DrawingCanvasView drawingView;
    private LinearLayout recallContainer;
    private LinearLayout imageContainer;
    private ImageView questionImage;
    private EditText imageAnswerInput;
    private Button prevButton;
    private Button nextButton;

    private final List<Question> questions = new ArrayList<>();
    private final HashMap<String, String> answers = new HashMap<>();
    private int currentIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mmse_quiz);

        questionTitle = findViewById(R.id.questionTitle);
        textInputContainer = findViewById(R.id.textInputContainer);
        textInput = findViewById(R.id.textInput);
        multipleChoiceContainer = findViewById(R.id.multipleChoiceContainer);
        radioGroup = findViewById(R.id.radioGroup);
        drawingContainer = findViewById(R.id.drawingContainer);
        drawingView = findViewById(R.id.drawingView);
        recallContainer = findViewById(R.id.recallContainer);
        imageContainer = findViewById(R.id.imageContainer);
        questionImage = findViewById(R.id.questionImage);
        imageAnswerInput = findViewById(R.id.imageAnswerInput);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);

        // Fetch and merge custom questions before starting quiz
        fetchAndMergeCustomQuestions();

        prevButton.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentIndex > 0) {
                currentIndex--;
                displayQuestion();
            }
        });

        nextButton.setOnClickListener(v -> {
            saveCurrentAnswer();
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                displayQuestion();
            } else {
                // Last question: score and save
                Map<String, Integer> sectionScores = new LinkedHashMap<>();
                StringBuilder feedback = new StringBuilder();
                int total = scoreAnswersWithFeedback(answers, sectionScores, feedback);
                String interpretation = interpret(total);

                MmseResult result = new MmseResult(
                        getPatientId(),
                        getCaregiverIdOptional(),
                        Timestamp.now(),
                        sectionScores,
                        total,
                        interpretation,
                        null
                );
                new MmseResultRepository().save(result, new MmseResultRepository.FirebaseCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showResult(total, interpretation, feedback.toString());
                    }

                    @Override
                    public void onError(String error) {
                        // Still navigate to result; consider showing a toast/log
                        showResult(total, interpretation, feedback.toString());
                    }
                });
            }
        });
    }
    // end of onCreate()

    private void fetchAndMergeCustomQuestions() {
        questions.clear();
        loadQuestionsFromJson();
        String patientId = getPatientId();
        if (patientId == null || patientId.isEmpty()) {
            resolvePlaceholders();
            randomizeVariants();
            if (questions.isEmpty()) {
                android.widget.Toast.makeText(this, "Unable to load MMSE questions", android.widget.Toast.LENGTH_LONG).show();
                questions.add(new Question("q1", "General", "What is today's date?", QuestionType.TEXT, null, null, null, 1));
            }
            displayQuestion();
            return;
        }
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("patients").document(patientId)
                .collection("custom_mmse_questions")
                .get()
                .addOnSuccessListener(query -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : query.getDocuments()) {
                        String id = doc.getId();
                        String section = "Custom";
                        String question = doc.getString("question");
                        String type = doc.getString("type");
                        int score = doc.contains("score") ? doc.getLong("score").intValue() : 1;
                        List<String> options = (List<String>) doc.get("options");
                        String expectedAnswer = doc.getString("expectedAnswer");
                        QuestionType qt = mapType(type);
                        Question q = new Question(id, section, question, qt,
                                options != null ? options.toArray(new String[0]) : null,
                                null, null, score);
                        try {
                            java.lang.reflect.Field f1 = Question.class.getDeclaredField("expectedAnswer");
                            f1.setAccessible(true);
                            f1.set(q, expectedAnswer);
                        } catch (Exception ignore) {}
                        questions.add(q);
                    }
                    resolvePlaceholders();
                    randomizeVariants();
                    if (questions.isEmpty()) {
                        android.widget.Toast.makeText(this, "Unable to load MMSE questions", android.widget.Toast.LENGTH_LONG).show();
                        questions.add(new Question("q1", "General", "What is today's date?", QuestionType.TEXT, null, null, null, 1));
                    }
                    displayQuestion();
                })
                .addOnFailureListener(e -> {
                    resolvePlaceholders();
                    randomizeVariants();
                    if (questions.isEmpty()) {
                        android.widget.Toast.makeText(this, "Unable to load MMSE questions", android.widget.Toast.LENGTH_LONG).show();
                        questions.add(new Question("q1", "General", "What is today's date?", QuestionType.TEXT, null, null, null, 1));
                    }
                    displayQuestion();
        });
    }
        // end of fetchAndMergeCustomQuestions()

    private void loadQuestionsFromJson() {
        questions.clear();
        try {
            InputStream is = getAssets().open("mmse_questions.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String json = sb.toString().trim();
            if (json.startsWith("[")) {
                parseQuestionsArray(new JSONArray(json));
            } else {
                JSONObject root = new JSONObject(json);
                if (root.has("questions")) {
                    parseQuestionsArray(root.getJSONArray("questions"));
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MmseQuizActivity", "Failed to load mmse_questions.json", e);
        }
    }

    private void parseQuestionsArray(JSONArray arr) throws JSONException {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String id = obj.optString("id", "q" + i);
            String section = obj.optString("section", "General");
            String question = obj.optString("question", "");
            String type = obj.optString("type", "text");
            int score = obj.optInt("score", 1);
            String[] options = null;
            if (obj.has("options")) {
                JSONArray opts = obj.optJSONArray("options");
                if (opts != null) {
                    options = new String[opts.length()];
                    for (int j = 0; j < opts.length(); j++) options[j] = opts.optString(j);
                }
            }
            List<String> expectedWords = null;
            if (obj.has("expectedWords")) {
                JSONArray words = obj.optJSONArray("expectedWords");
                if (words != null) {
                    expectedWords = new ArrayList<>();
                    for (int j = 0; j < words.length(); j++) expectedWords.add(words.optString(j));
                }
            }
            String imageUrl = obj.optString("imageUrl", null);
            QuestionType qt = mapType(type);
            Question q = new Question(id, section, question, qt, options, expectedWords, imageUrl, score);
            // assign optional answer metadata
            try {
                java.lang.reflect.Field f1 = Question.class.getDeclaredField("expectedAnswer");
                f1.setAccessible(true);
                f1.set(q, obj.optString("expectedAnswer", null));
                java.lang.reflect.Field f2 = Question.class.getDeclaredField("acceptedAnswers");
                f2.setAccessible(true);
                if (obj.has("acceptedAnswers")) {
                    JSONArray arrAcc = obj.optJSONArray("acceptedAnswers");
                    if (arrAcc != null) {
                        List<String> acc = new ArrayList<>();
                        for (int j = 0; j < arrAcc.length(); j++) acc.add(arrAcc.optString(j));
                        f2.set(q, acc);
                    }
                }
                java.lang.reflect.Field f3 = Question.class.getDeclaredField("correctOption");
                f3.setAccessible(true);
                f3.set(q, obj.optString("correctOption", null));
            } catch (Exception ignore) {}
            questions.add(q);
        }
    }

    private void resolvePlaceholders() {
        java.time.LocalDate now = java.time.LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = now.getMonth().name().toLowerCase();
        String weekday = now.getDayOfWeek().name().toLowerCase();
        String country = getDeviceCountry().toLowerCase();
        for (Question q : questions) {
            if (q.expectedAnswer != null) {
                String ea = q.expectedAnswer;
                ea = ea.replace("{{CURRENT_YEAR}}", year)
                       .replace("{{CURRENT_MONTH_NAME}}", month)
                       .replace("{{CURRENT_WEEKDAY}}", weekday)
                       .replace("{{CURRENT_COUNTRY}}", country);
                try {
                    java.lang.reflect.Field f = Question.class.getDeclaredField("expectedAnswer");
                    f.setAccessible(true);
                    f.set(q, ea);
                } catch (Exception ignore) {}
            }
        }
    }

    private String getDeviceCountry() {
        try {
            java.util.Locale locale = getResources().getConfiguration().getLocales().get(0);
            String country = new java.util.Locale("", locale.getCountry()).getDisplayCountry();
            return country == null ? "" : country;
        } catch (Exception e) {
            return "";
        }
    }

    private void randomizeVariants() {
        // Randomize registration/recall words ordering
        for (Question q : questions) {
            if (q.type == QuestionType.RECALL && q.expectedWords != null && q.expectedWords.size() > 1) {
                java.util.Collections.shuffle(q.expectedWords);
            }
        }
        // Randomize naming images between a known set if present
        String[] namingDrawables = new String[]{"drawable:ic_clock","drawable:ic_phone","drawable:ic_person"};
        for (Question q : questions) {
            if (q.type == QuestionType.IMAGE && (q.imageUrl == null || q.imageUrl.startsWith("drawable:"))) {
                if (namingDrawables != null && namingDrawables.length > 0) {
                    q.imageUrl = namingDrawables[new java.util.Random().nextInt(namingDrawables.length)];
                } else {
                    // fallback to a default drawable or leave null
                    q.imageUrl = "drawable:ic_default";
                }
            }
        }
    }

    private void displayQuestion() {
        if (questions.isEmpty() || currentIndex < 0 || currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);
        questionTitle.setText(q.title);
        textInputContainer.setVisibility(View.GONE);
        multipleChoiceContainer.setVisibility(View.GONE);
        drawingContainer.setVisibility(View.GONE);
        recallContainer.setVisibility(View.GONE);
        imageContainer.setVisibility(View.GONE);

        if (q.type == QuestionType.TEXT) {
            textInputContainer.setVisibility(View.VISIBLE);
            String prev = answers.get(q.id);
            textInput.setText(prev != null ? prev : "");
        } else if (q.type == QuestionType.CHOICE) {
            multipleChoiceContainer.setVisibility(View.VISIBLE);
            radioGroup.removeAllViews();
            for (String option : q.options != null ? q.options : new String[0]) {
                RadioButton rb = new RadioButton(this);
                rb.setText(option);
                radioGroup.addView(rb);
                String prev = answers.get(q.id);
                if (!TextUtils.isEmpty(prev) && prev.equals(option)) {
                    rb.setChecked(true);
                }
            }
        } else if (q.type == QuestionType.DRAW) {
            drawingContainer.setVisibility(View.VISIBLE);
            drawingView.clear();
        } else if (q.type == QuestionType.RECALL) {
            recallContainer.setVisibility(View.VISIBLE);
            recallContainer.removeAllViews();
            int count = q.expectedWords != null ? q.expectedWords.size() : 3;
            String prev = answers.get(q.id);
            String[] prevParts = prev != null ? prev.split(",") : new String[0];
            for (int i = 0; i < count; i++) {
                EditText et = new EditText(this);
                et.setHint("Word " + (i + 1));
                if (i < prevParts.length) et.setText(prevParts[i]);
                recallContainer.addView(et);
            }
        } else if (q.type == QuestionType.IMAGE) {
            imageContainer.setVisibility(View.VISIBLE);
            imageAnswerInput.setText(answers.get(q.id));
            try {
                if (!TextUtils.isEmpty(q.imageUrl)) {
                    if (q.imageUrl.startsWith("http")) {
                        // For simplicity, skip remote loading without Glide/Picasso
                    } else if (q.imageUrl.startsWith("drawable:")) {
                        String name = q.imageUrl.substring("drawable:".length());
                        int resId = getResources().getIdentifier(name, "drawable", getPackageName());
                        if (resId != 0) questionImage.setImageResource(resId);
                        else questionImage.setImageDrawable(null);
                    } else {
                        java.io.InputStream is = getAssets().open(q.imageUrl);
                        android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(is);
                        questionImage.setImageBitmap(bmp);
                        is.close();
                    }
                } else {
                    questionImage.setImageDrawable(null);
                }
            } catch (Exception ignore) {}
        }

        prevButton.setEnabled(currentIndex > 0);
        nextButton.setText(currentIndex == questions.size() - 1 ? R.string.submit : R.string.next);
    }

    private void saveCurrentAnswer() {
        if (questions.isEmpty() || currentIndex < 0 || currentIndex >= questions.size()) return;
        Question q = questions.get(currentIndex);
        if (q.type == QuestionType.TEXT) {
            answers.put(q.id, textInput.getText().toString().trim());
        } else if (q.type == QuestionType.CHOICE) {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId != -1) {
                RadioButton rb = findViewById(checkedId);
                answers.put(q.id, rb.getText().toString());
            }
        } else if (q.type == QuestionType.DRAW) {
            // For drawing, save a simple flag ("done")
            answers.put(q.id, drawingView.isEmpty() ? "" : "done");
        } else if (q.type == QuestionType.RECALL) {
            int count = recallContainer.getChildCount();
            List<String> words = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                View child = recallContainer.getChildAt(i);
                if (child instanceof EditText) {
                    words.add(((EditText) child).getText().toString().trim());
                }
            }
            answers.put(q.id, TextUtils.join(",", words));
        } else if (q.type == QuestionType.IMAGE) {
            answers.put(q.id, imageAnswerInput.getText().toString().trim());
        }
    }

    private int scoreAnswers(HashMap<String, String> answers, Map<String, Integer> sectionScores) {
        int total = 0;
        sectionScores.clear();
        for (Question q : questions) {
            int awarded = 0;
            String ans = answers.get(q.id);
            switch (q.type) {
                case TEXT:
                case IMAGE:
                    if (!TextUtils.isEmpty(ans)) awarded = q.score;
                    break;
                case CHOICE:
                    if (!TextUtils.isEmpty(ans)) awarded = q.score;
                    break;
                case RECALL:
                    if (!TextUtils.isEmpty(ans)) {
                        String[] parts = ans.split(",");
                        int nonEmpty = 0;
                        for (String p : parts) if (!TextUtils.isEmpty(p)) nonEmpty++;
                        awarded = Math.min(nonEmpty, Math.max(1, q.score));
                    }
                    break;
                case DRAW:
                    if ("done".equals(ans)) awarded = q.score;
                    break;
            }
            total += awarded;
            String sec = TextUtils.isEmpty(q.section) ? "General" : q.section;
            Integer cur = sectionScores.get(sec);
            sectionScores.put(sec, (cur == null ? 0 : cur) + awarded);
        }
        // Rescale total to 30 if needed
        int rawMax = 0;
        for (Question q : questions) rawMax += Math.max(1, q.score);
        if (rawMax > 0 && rawMax != 30) {
            total = Math.round((total / (float) rawMax) * 30f);
        }
        return total;
    }

    private int scoreAnswersWithFeedback(HashMap<String, String> answers, Map<String, Integer> sectionScores, StringBuilder feedback) {
        int total = 0;
        sectionScores.clear();
        for (Question q : questions) {
            int awarded = 0;
            String ans = answers.get(q.id);
            String normalized = ans == null ? "" : ans.trim().toLowerCase();
            boolean correct = false;
            switch (q.type) {
                case TEXT:
                case IMAGE:
                    if (!TextUtils.isEmpty(q.expectedAnswer)) {
                        correct = normalized.equals(q.expectedAnswer.trim().toLowerCase());
                    } else if (q.acceptedAnswers != null) {
                        for (String a : q.acceptedAnswers) {
                            if (normalized.equals(a.trim().toLowerCase())) { correct = true; break; }
                        }
                    } else {
                        correct = !TextUtils.isEmpty(normalized);
                    }
                    if (correct) awarded = q.score;
                    break;
                case CHOICE:
                    if (!TextUtils.isEmpty(q.correctOption)) {
                        correct = normalized.equals(q.correctOption.trim().toLowerCase());
                    } else {
                        correct = !TextUtils.isEmpty(normalized);
                    }
                    if (correct) awarded = q.score;
                    break;
                case RECALL:
                    int nonEmpty = 0;
                    if (!TextUtils.isEmpty(ans)) {
                        String[] parts = ans.split(",");
                        for (String p : parts) if (!TextUtils.isEmpty(p.trim())) nonEmpty++;
                    }
                    awarded = Math.min(nonEmpty, Math.max(1, q.score));
                    correct = awarded == q.score;
                    break;
                case DRAW:
                    correct = "done".equals(ans);
                    if (correct) awarded = q.score;
                    break;
            }
            total += awarded;
            String sec = TextUtils.isEmpty(q.section) ? "General" : q.section;
            Integer cur = sectionScores.get(sec);
            sectionScores.put(sec, (cur == null ? 0 : cur) + awarded);

            feedback.append(q.title)
                    .append(" — ")
                    .append(correct ? "Correct" : "Incorrect")
                    .append(" (+")
                    .append(awarded)
                    .append(")\n");
        }
        // Rescale total to 30 if needed
        int rawMax = 0;
        for (Question q : questions) rawMax += Math.max(1, q.score);
        if (rawMax > 0 && rawMax != 30) {
            total = Math.round((total / (float) rawMax) * 30f);
        }
        return total;
    }

    private String interpret(int total) {
        if (total >= 24) return "Normal";
        if (total >= 18) return "Mild Impairment";
        if (total >= 10) return "Moderate";
        return "Severe";
    }

    private void showResult(int total, String interpretation, String feedback) {
        // If this quiz was launched from a schedule, mark it as completed
        String scheduleId = getIntent().getStringExtra("scheduleId");
        if (scheduleId != null) {
            String patientId = getPatientId();
            com.mihir.alzheimerscaregiver.mmse.MmseScheduleManager.markCompleted(patientId, scheduleId);
        }
        Intent intent = new Intent(this, MmseResultActivity.class);
        intent.putExtra("score", total);
        intent.putExtra("interpretation", interpretation);
        intent.putExtra("feedback", feedback);
        startActivity(intent);
        finish();
    }

    @Nullable
    private String getCaregiverIdOptional() {
        return null; // Extend to pass caregiver id if available in your app
    }

    private String getPatientId() {
        // If you store patient id as current user, adjust accordingly
        return com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "default";
    }

    private enum QuestionType { TEXT, CHOICE, DRAW, RECALL, IMAGE }

    private static QuestionType mapType(String type) {
        if ("multiple_choice".equalsIgnoreCase(type)) return QuestionType.CHOICE;
        if ("drawing".equalsIgnoreCase(type)) return QuestionType.DRAW;
        if ("recall".equalsIgnoreCase(type)) return QuestionType.RECALL;
        if ("image".equalsIgnoreCase(type)) return QuestionType.IMAGE;
        return QuestionType.TEXT;
    }

    private static class Question {
    final String id;
    final String section;
    final String title;
    final QuestionType type;
    final String[] options;
    final List<String> expectedWords;
    String imageUrl;
    final int score;
    final String expectedAnswer;
    final List<String> acceptedAnswers;
    final String correctOption;

        private Question(String id, String section, String title, QuestionType type, String[] options, List<String> expectedWords, String imageUrl, int score) {
            this.id = id;
            this.section = section;
            this.title = title;
            this.type = type;
            this.options = options;
            this.expectedWords = expectedWords;
            this.imageUrl = imageUrl;
            this.score = score;
            this.expectedAnswer = null;
            this.acceptedAnswers = null;
            this.correctOption = null;
        }
    }
}


