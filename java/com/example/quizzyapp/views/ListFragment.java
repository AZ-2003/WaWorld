package com.example.quizzyapp.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.quizzyapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ListFragment extends Fragment {

    private TextView hiPlayerNameText;
    private TextView levelText;
    private TextView experienceText;
    private TextView xpToNextLevelText;
    private Button playButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        hiPlayerNameText = view.findViewById(R.id.hiPlayerNameText);
        levelText = view.findViewById(R.id.levelValue);
        experienceText = view.findViewById(R.id.XPValue);
        xpToNextLevelText = view.findViewById(R.id.nextLevelValue);
        playButton = view.findViewById(R.id.playButton);

        // Get current Firebase user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Reference to the database
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("Users")  // Ensure this matches your Firebase structure
                    .child(userId);

            // Retrieve player data from Firebase
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String playerName = snapshot.child("playerName").getValue(String.class);
                        int currentXP = snapshot.child("experience").getValue(Integer.class);
                        int currentLevel = snapshot.child("level").getValue(Integer.class);

                        // Calculate XP to next level
                        int xpToNextLevel = calculateXPToNextLevel(currentXP);

                        // Update UI
                        hiPlayerNameText.setText("Hi, " + playerName);
                        levelText.setText(String.valueOf(currentLevel));
                        experienceText.setText(String.valueOf(currentXP));
                        xpToNextLevelText.setText(String.valueOf(xpToNextLevel));
                    } else {
                        Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "No user is signed in", Toast.LENGTH_SHORT).show();
        }

        // Set up navigation to the ContinentFragment when the play button is clicked
        NavController navController = Navigation.findNavController(view);
        playButton.setOnClickListener(v -> navController.navigate(R.id.action_listFragment_to_continentFragment));
    }

    private int calculateXPToNextLevel(int totalXP) {
        int baseXP = 100; // Minimum XP for Level 2
        int level = 1;
        int xpForNextLevel = baseXP;

        // Calculate XP required for the next level
        while (totalXP >= xpForNextLevel) {
            totalXP -= xpForNextLevel;
            level++;
            xpForNextLevel = (int) (baseXP * Math.pow(level, 1.3)); // Scaling factor is 1.3
        }

        return xpForNextLevel - totalXP; // XP required to reach the next level
    }
}
