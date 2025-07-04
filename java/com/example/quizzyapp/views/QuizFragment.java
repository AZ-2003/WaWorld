package com.example.quizzyapp.views;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.quizzyapp.R;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Map;

public class QuizFragment extends Fragment {

    private static final long TIMER_DURATION = 15000; // 30 seconds
    private FirebaseFirestore db;
    private ArrayList<Question> questions;

    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;

    private TextView questionTextView, quizQuestionsCount;
    private ProgressBar quizCountProgressBar;
    private Button option1Button, option2Button, option3Button, option4Button, nextQuestionButton;
    private String selectedDifficulty;
    private CountDownTimer timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);

        // Initialize views
        initializeUI(rootView);

        // Retrieve arguments
        String selectedContinent = getArguments().getString("continent");
        selectedDifficulty = getArguments().getString("difficulty");

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        questions = new ArrayList<>();

        // Load questions from Firestore
        loadQuestionsFromFirestore(selectedContinent, selectedDifficulty);

        // Handle Pause button click
        ImageButton pauseButton = rootView.findViewById(R.id.pauseBtn);
        pauseButton.setOnClickListener(v -> showPauseDialog());

        return rootView;
    }

    private void initializeUI(View rootView) {
        questionTextView = rootView.findViewById(R.id.quizQuestionTv);
        quizQuestionsCount = rootView.findViewById(R.id.quizQuestionsCount);
        quizCountProgressBar = rootView.findViewById(R.id.countTimeQuiz);

        option1Button = rootView.findViewById(R.id.option1Btn);
        option2Button = rootView.findViewById(R.id.option2Btn);
        option3Button = rootView.findViewById(R.id.option3Btn);
        option4Button = rootView.findViewById(R.id.option4Btn);
        nextQuestionButton = rootView.findViewById(R.id.nextQueBtn);

        // Set click listeners for options
        setButtonClickListener(option1Button, 1);
        setButtonClickListener(option2Button, 2);
        setButtonClickListener(option3Button, 3);
        setButtonClickListener(option4Button, 4);

        // Handle next question button
        nextQuestionButton.setOnClickListener(v -> {
            currentQuestionIndex++;
            displayNextQuestion();
        });
    }

    private void setButtonClickListener(Button button, int option) {
        button.setOnClickListener(view -> {
            resetButtonColors();
            checkAnswer(option);
            disableOptions();
        });
    }

    private void loadQuestionsFromFirestore(String continent, String difficulty) {
        db.collection("WaWorld_quiz")
                .document(continent)
                .collection(difficulty)
                .document("question")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> questionsMap = (Map<String, Object>) documentSnapshot.get("question");
                        if (questionsMap != null) {
                            parseQuestionsData(questionsMap);
                        }
                    }
                    displayNextQuestion();
                })
                .addOnFailureListener(e -> Log.e("QuizFragment", "Error loading questions", e));
    }

    private void parseQuestionsData(Map<String, Object> questionsMap) {
        for (Map.Entry<String, Object> entry : questionsMap.entrySet()) {
            Map<String, Object> questionData = (Map<String, Object>) entry.getValue();
            String questionText = (String) questionData.get("desc");
            String[] options = {
                    (String) questionData.get("option1"),
                    (String) questionData.get("option2"),
                    (String) questionData.get("option3"),
                    (String) questionData.get("option4")
            };
            String correctAnswer = (String) questionData.get("answer");
            int correctOption = getCorrectOptionIndex(options, correctAnswer);

            questions.add(new Question(questionText, options, correctOption));
        }
    }

    private int getCorrectOptionIndex(String[] options, String correctAnswer) {
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(correctAnswer)) {
                return i + 1; // Option indexes are 1-based.
            }
        }
        return 0;
    }

    private void displayNextQuestion() {
        if (currentQuestionIndex < questions.size()) {
            resetButtonColors();
            Question currentQuestion = questions.get(currentQuestionIndex);
            updateQuestionUI(currentQuestion);
        } else {
            endQuiz();
        }
    }

    private void showPauseDialog() {
        // Create a custom dialog layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.pause_menu, null);

        // Find dialog buttons in the custom layout
        Button retryButton = dialogView.findViewById(R.id.retryButton);
        Button backHomeButton = dialogView.findViewById(R.id.backHomeButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        // Build the dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // Show the dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

        // Handle Retry button click
        retryButton.setOnClickListener(v -> {
            dialog.dismiss();
            restartQuiz();
        });

        // Handle Back Home button click
        backHomeButton.setOnClickListener(v -> {
            dialog.dismiss();
            Navigation.findNavController(requireView()).navigate(R.id.action_quizFragment_to_listFragment);
        });

        // Handle Cancel button click
        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    // Method to restart the quiz
    private void restartQuiz() {
        // Reset quiz state
        currentQuestionIndex = 0;
        correctAnswers = 0;
        incorrectAnswers = 0;
        questions.clear();
        resetTimer();

        // Reload questions and reset UI
        loadQuestionsFromFirestore(
                getArguments().getString("continent"),
                getArguments().getString("difficulty")
        );
    }

    private void updateQuestionUI(Question currentQuestion) {
        questionTextView.setText(currentQuestion.getQuestionText());
        String[] options = currentQuestion.getOptions();

        option1Button.setText(options[0]);
        option2Button.setText(options[1]);
        option3Button.setText(options[2]);
        option4Button.setText(options[3]);

        quizQuestionsCount.setText(String.format("%d/%d", currentQuestionIndex + 1, questions.size()));

        resetTimer();
        startTimer();

        enableOptions();
        disableNextQuestionButton();
    }

    private void checkAnswer(int selectedOption) {
        Question currentQuestion = questions.get(currentQuestionIndex);
        Button selectedButton = getSelectedButton(selectedOption);
        if (selectedOption == currentQuestion.getCorrectOption()) {
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            correctAnswers++;
        } else {
            selectedButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            Button correctButton = getSelectedButton(currentQuestion.getCorrectOption());
            correctButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            incorrectAnswers++;
        }

        enableNextQuestionButton();
    }

    private Button getSelectedButton(int option) {
        switch (option) {
            case 1: return option1Button;
            case 2: return option2Button;
            case 3: return option3Button;
            case 4: return option4Button;
            default: return null;
        }
    }

    private void endQuiz() {
        questionTextView.setText("Quiz Over! Your Score: " + correctAnswers + "/" + questions.size());
        hideQuizUI();
        navigateToResultFragment();
    }

    private void hideQuizUI() {
        option1Button.setVisibility(View.GONE);
        option2Button.setVisibility(View.GONE);
        option3Button.setVisibility(View.GONE);
        option4Button.setVisibility(View.GONE);
        nextQuestionButton.setVisibility(View.GONE);
        quizCountProgressBar.setVisibility(View.GONE);
    }

    private void navigateToResultFragment() {
        Bundle resultBundle = new Bundle();
        resultBundle.putInt("correctAnswers", correctAnswers);
        resultBundle.putInt("wrongAnswers", incorrectAnswers);
        resultBundle.putInt("notAnswered", 0);
        resultBundle.putString("difficulty", selectedDifficulty);     // Pass the difficulty

        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(R.id.action_quizFragment_to_resultFragment, resultBundle);
        } else {
            Log.e("QuizFragment", "Navigation failed: view is null");
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) ((TIMER_DURATION - millisUntilFinished) * 100 / TIMER_DURATION);
                quizCountProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                quizCountProgressBar.setProgress(100);
                currentQuestionIndex++;
                displayNextQuestion();
            }
        }.start();
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        quizCountProgressBar.setProgress(0);
    }

    private void disableOptions() {
        option1Button.setEnabled(false);
        option2Button.setEnabled(false);
        option3Button.setEnabled(false);
        option4Button.setEnabled(false);
    }

    private void enableOptions() {
        option1Button.setEnabled(true);
        option2Button.setEnabled(true);
        option3Button.setEnabled(true);
        option4Button.setEnabled(true);
    }

    private void enableNextQuestionButton() {
        nextQuestionButton.setEnabled(true);
    }

    private void disableNextQuestionButton() {
        nextQuestionButton.setEnabled(false);
    }

    private void resetButtonColors() {
        option1Button.setBackgroundResource(R.drawable.default_button_background);
        option2Button.setBackgroundResource(R.drawable.default_button_background);
        option3Button.setBackgroundResource(R.drawable.default_button_background);
        option4Button.setBackgroundResource(R.drawable.default_button_background);
    }
}
