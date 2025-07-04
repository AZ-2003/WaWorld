package com.example.quizzyapp.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.lifecycle.ViewModelProvider;
import com.example.quizzyapp.views.ResultViewModel; // Adjust the import path if necessary

import com.example.quizzyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

public class ResultFragment extends Fragment {
    private static final String TAG = "ResultFragment";
    private int correctAnswers;
    private int wrongAnswers;
    private TextView correctAnswerTv;
    private TextView wrongAnswersTv;
    private TextView performanceMessageTv;
    private ResultViewModel viewModel; // Declare the ViewModel

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ResultViewModel.class); // Initialize the ViewModel

        correctAnswerTv = view.findViewById(R.id.correctAnswerTv);
        wrongAnswersTv = view.findViewById(R.id.wrongAnswersTv);
        performanceMessageTv = view.findViewById(R.id.performanceMessageTv);
        Button playAgainBtn = view.findViewById(R.id.playAgain_btn);
        Button homeBtn = view.findViewById(R.id.homeBtn);
        Button viewLeaderboardBtn = view.findViewById(R.id.viewLeaderboardBtn);

        String difficulty = "easy"; // Default difficulty
        if (getArguments() != null) {
            correctAnswers = getArguments().getInt("correctAnswers", 0);
            wrongAnswers = getArguments().getInt("wrongAnswers", 0);
            difficulty = getArguments().getString("difficulty", "easy");
            int xpEarned = calculateExperiencePoints(correctAnswers);
            saveExperienceToFirebase(xpEarned);
        }

        updateResultUI();
        showPerformanceMessage();

        // Save the score immediately when the fragment is displayed
        if (!viewModel.isScoreSaved()) {
            saveScoreToFirebase(difficulty);
        }

        // Set up button click listeners for navigation
        homeBtn.setOnClickListener(v -> navigateToListFragment(view));
        playAgainBtn.setOnClickListener(v -> navigateToContinentFragment(view));
        viewLeaderboardBtn.setOnClickListener(v -> navigateToLeaderboardFragment(view));

        // Handle back button press
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog(view);
            }
        });
    }

    private void updateResultUI() {
        correctAnswerTv.setText("Correct Answers: " + correctAnswers);
        wrongAnswersTv.setText("Wrong Answers: " + wrongAnswers);
    }

    private void showPerformanceMessage() {
        int totalQuestions = correctAnswers + wrongAnswers;
        if (totalQuestions == 0) {
            performanceMessageTv.setText("No questions answered.");
            return;
        }

        int percentage = (int) ((correctAnswers / (float) totalQuestions) * 100);
        if (percentage > 80) {
            performanceMessageTv.setText("Great Job!");
        } else if (percentage >= 50) {
            performanceMessageTv.setText("Good Effort!");
        } else {
            performanceMessageTv.setText("Better Luck Next Time!");
        }
    }

    private void navigateToContinentFragment(View view) {
        Navigation.findNavController(view).navigate(R.id.action_resultFragment_to_continentFragment);
    }

    private void navigateToListFragment(View view) {
        Navigation.findNavController(view).navigate(R.id.action_resultFragment_to_listFragment);
    }

    private void navigateToLeaderboardFragment(View view) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt("correctAnswers", correctAnswers); // Pass any required data
            Navigation.findNavController(view).navigate(R.id.action_resultFragment_to_leaderboardFragment, bundle);
        } catch (Exception e) {
            Log.e(TAG, "Navigation to LeaderboardFragment failed: " + e.getMessage());
            Toast.makeText(getContext(), "Unable to navigate to the leaderboard. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }
    private int calculateExperiencePoints(int correctAnswers) {
        return correctAnswers * 10; // 10 XP per correct answer
    }
    private int calculateLevel(int totalXP) {
        int baseXP = 100; // Minimum XP for Level 2
        int level = 1;
        int xpForNextLevel = baseXP;

        while (totalXP >= xpForNextLevel) {
            totalXP -= xpForNextLevel;
            level++;
            xpForNextLevel = (int) (baseXP * Math.pow(level, 1.3)); // Scaling factor is 1.5
        }

        return level;
    }
    private void saveExperienceToFirebase(int xpEarned) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId);

            userRef.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer currentXP = currentData.child("experience").getValue(Integer.class);
                    Integer currentLevel = currentData.child("level").getValue(Integer.class);

                    if (currentXP == null) currentXP = 0;
                    if (currentLevel == null) currentLevel = 1;

                    currentXP += xpEarned; // Add earned XP

                    // Update level and XP for next level
                    int newLevel = calculateLevel(currentXP);
                    currentData.child("experience").setValue(currentXP);
                    currentData.child("level").setValue(newLevel);

                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                    if (error != null) {
                        Log.e("ExperienceSave", "Error saving experience: " + error.getMessage());
                    } else if (committed) {
                        Log.d("ExperienceSave", "Experience and level updated successfully!");
                    }
                }
            });
        } else {
            Log.e("ExperienceSave", "User is not authenticated.");
        }
    }

    private void saveScoreToFirebase(String difficulty) {
        // Adjust score based on difficulty
        int baseScore = getScoreMultiplier(difficulty);
        int score = correctAnswers * baseScore;

        DatabaseReference scoreRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("score");

        scoreRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentScore = currentData.getValue(Integer.class);

                if (currentScore == null) {
                    currentScore = 0; // Initialize if null
                }
                currentData.setValue(currentScore + score); // Add the new score
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot currentData) {
                if (databaseError != null) {
                    Log.e(TAG, "Error saving score: " + databaseError.getMessage());
                    Toast.makeText(getContext(), "Failed to save score. Please try again.", Toast.LENGTH_SHORT).show();
                } else if (committed) {
                    Log.d(TAG, "Score saved successfully!");
                    Toast.makeText(getContext(), "Score saved successfully!", Toast.LENGTH_SHORT).show();
                    viewModel.setScoreSaved(true); // Mark score as saved
                }
            }
        });
    }
    private int getScoreMultiplier(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "medium":
                return 20; // Example multiplier for medium difficulty
            case "hard":
                return 30; // Example multiplier for hard difficulty
            case "easy":
            default:
                return 10; // Default multiplier for easy difficulty
        }
    }
    private void showExitConfirmationDialog(View view) {
        // Create a confirmation dialog
        new AlertDialog.Builder(getContext())
                .setMessage("Are you sure you want to go back to the main screen?")
                .setCancelable(false) // Prevent cancellation by tapping outside
                .setPositiveButton("Yes", (dialog, id) -> {
                    // If user confirms, navigate back to ListFragment
                    navigateToListFragment(view);
                })
                .setNegativeButton("No", (dialog, id) -> {
                    // If user cancels, just dismiss the dialog
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}


