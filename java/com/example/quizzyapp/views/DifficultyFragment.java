package com.example.quizzyapp.views;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quizzyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DifficultyFragment extends Fragment {
    private ImageView continentImageView;
    private Button easyButton, mediumButton, hardButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_difficulty, container, false);

        // Retrieve the continent from arguments
        String selectedContinent = getArguments().getString("continent");
        continentImageView = rootView.findViewById(R.id.continentImageView);

        switch (selectedContinent) {
            case "Africa":
                continentImageView.setImageResource(R.drawable.africa); // Replace with your drawable resource
                break;
            case "Asia":
                continentImageView.setImageResource(R.drawable.asia);
                break;
            case "Europe":
                continentImageView.setImageResource(R.drawable.europe);
                break;
            case "North America":
                continentImageView.setImageResource(R.drawable.north_america);
                break;
            case "South America":
                continentImageView.setImageResource(R.drawable.south_america);
                break;
            case "Antarctica":
                continentImageView.setImageResource(R.drawable.antarctica);
                break;
            case "Australia":
                continentImageView.setImageResource(R.drawable.australia);
                break;
            default:
                continentImageView.setImageResource(R.drawable.splash);
                break;
        }

        // Initialize buttons for each difficulty level
        easyButton = rootView.findViewById(R.id.buttonEasy);
        mediumButton = rootView.findViewById(R.id.buttonMedium);
        hardButton = rootView.findViewById(R.id.buttonHard);

        // Retrieve the user's level from Firebase
        checkUserLevel();

        // Set up click listeners for each difficulty button
        View.OnClickListener difficultyClickListener = view -> {
            Bundle bundle = new Bundle();
            String selectedDifficulty = "";

            if (view == easyButton) selectedDifficulty = "Easy";
            if (view == mediumButton) selectedDifficulty = "Medium";
            if (view == hardButton) selectedDifficulty = "Hard";

            // Pass both the continent and difficulty to the QuizFragment
            bundle.putString("continent", selectedContinent);
            bundle.putString("difficulty", selectedDifficulty);

            // Navigate to QuizFragment with the selected continent and difficulty
            Navigation.findNavController(view).navigate(R.id.action_difficultyFragment_to_quizFragment, bundle);
        };

        easyButton.setOnClickListener(difficultyClickListener);
        mediumButton.setOnClickListener(difficultyClickListener);
        hardButton.setOnClickListener(difficultyClickListener);

        return rootView;
    }

    private void checkUserLevel() {
        // Get the current user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch user data to get the current level
        userRef.child("level").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer currentLevel = dataSnapshot.getValue(Integer.class);

                if (currentLevel != null) {
                    unlockDifficultyLevels(currentLevel);
                } else {
                    Toast.makeText(getContext(), "Error retrieving user level.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unlockDifficultyLevels(int userLevel) {
        // Unlock difficulties based on user's level and update button text
        if (userLevel >= 3) {
            mediumButton.setEnabled(true);
            mediumButton.setText("Medium"); // Update button text
        } else {
            mediumButton.setEnabled(false);
            mediumButton.setText("Medium (Requires Level 3)"); // Locked, show level requirement
        }

        if (userLevel >= 5) {
            hardButton.setEnabled(true);
            hardButton.setText("Hard"); // Update button text
        } else {
            hardButton.setEnabled(false);
            hardButton.setText("Hard (Requires Level 5)"); // Locked, show level requirement
        }

        // Easy difficulty is always available
        easyButton.setText("Easy");
    }
}