package com.example.quizzyapp.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quizzyapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardFragment extends Fragment {
    private ListView leaderboardListView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leaderboardListView = view.findViewById(R.id.leaderboardListView);

        // Reference to the 'Users' node in Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Integer> playerScores = new HashMap<>();

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String playerName = userSnapshot.child("playerName").getValue(String.class);
                        Integer score = userSnapshot.child("score").getValue(Integer.class);

                        if (playerName != null && score != null) {
                            // Use containsKey to check if player already exists in the map
                            if (playerScores.containsKey(playerName)) {
                                playerScores.put(playerName, playerScores.get(playerName) + score);
                            } else {
                                playerScores.put(playerName, score);
                            }
                        }
                    }

                    List<Map.Entry<String, Integer>> leaderboardEntries = new ArrayList<>(playerScores.entrySet());
                    Collections.sort(leaderboardEntries, (a, b) -> Integer.compare(b.getValue(), a.getValue()));

                    List<String> topThreePlayers = new ArrayList<>();
                    for (int i = 0; i < Math.min(3, leaderboardEntries.size()); i++) {
                        Map.Entry<String, Integer> entry = leaderboardEntries.get(i);
                        topThreePlayers.add(entry.getKey() + ": " + entry.getValue());
                    }

                    List<String> remainingPlayers = new ArrayList<>();
                    for (int i = 3; i < leaderboardEntries.size(); i++) {
                        Map.Entry<String, Integer> entry = leaderboardEntries.get(i);
                        remainingPlayers.add(entry.getKey() + ": " + entry.getValue());
                    }

                    updateTopThreeUI(topThreePlayers);

                    CustomAdapter adapter = new CustomAdapter(getContext(), remainingPlayers);
                    leaderboardListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading leaderboard: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Utility method to update the top 3 players' UI
    private void updateTopThreeUI(List<String> topThreePlayers) {
        if (getView() == null) return;

        // Find views for the top 3 players
        View firstPlaceView = getView().findViewById(R.id.firstPlaceContainer);
        View secondPlaceView = getView().findViewById(R.id.secondPlaceContainer);
        View thirdPlaceView = getView().findViewById(R.id.thirdPlaceContainer);

        // Update the first place
        if (!topThreePlayers.isEmpty() && firstPlaceView != null) {
            String[] firstPlace = topThreePlayers.get(0).split(": ");
            ((TextView) firstPlaceView.findViewById(R.id.firstPlaceName)).setText(firstPlace[0]);
            ((TextView) firstPlaceView.findViewById(R.id.firstPlaceScore)).setText(firstPlace[1]);
        }

        // Update the second place
        if (topThreePlayers.size() > 1 && secondPlaceView != null) {
            String[] secondPlace = topThreePlayers.get(1).split(": ");
            ((TextView) secondPlaceView.findViewById(R.id.secondPlaceName)).setText(secondPlace[0]);
            ((TextView) secondPlaceView.findViewById(R.id.secondPlaceScore)).setText(secondPlace[1]);
        }

        // Update the third place
        if (topThreePlayers.size() > 2 && thirdPlaceView != null) {
            String[] thirdPlace = topThreePlayers.get(2).split(": ");
            ((TextView) thirdPlaceView.findViewById(R.id.thirdPlaceName)).setText(thirdPlace[0]);
            ((TextView) thirdPlaceView.findViewById(R.id.thirdPlaceScore)).setText(thirdPlace[1]);
        }
    }
}
