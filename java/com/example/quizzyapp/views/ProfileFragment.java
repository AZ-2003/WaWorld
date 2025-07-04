package com.example.quizzyapp.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzyapp.R;
import com.example.quizzyapp.MainActivity;  // Add this import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {

    private TextView userEmail, playerName;
    private Button logoutButton, deleteAccountButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userEmail = view.findViewById(R.id.userEmail);
        playerName = view.findViewById(R.id.playerName);
        logoutButton = view.findViewById(R.id.logoutButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            userEmail.setText("Email: " + currentUser.getEmail());
            playerName.setText("Player Name: " + currentUser.getDisplayName());
        }

        // Logout Button
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to SplashFragment after logout
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_profileFragment_to_splashFragment);
        });

        // Delete Account Button
        deleteAccountButton.setOnClickListener(v -> {
            if (currentUser != null) {
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(currentUser.getUid());

                // Delete user data from Realtime Database
                userRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Delete the user account
                        currentUser.delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();

                                // Restart the app and navigate to MainActivity (or SplashActivity)
                                if (getActivity() != null) {
                                    Intent intent = new Intent(getActivity(), MainActivity.class);  // Ensure MainActivity import is here
                                    intent.putExtra("RESET_APP", true);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    getActivity().finish();
                                }

                            } else {
                                Toast.makeText(getContext(), "Failed to delete account: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("ProfileFragment", "Account deletion error: " + deleteTask.getException().getMessage());
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to delete user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileFragment", "User data deletion error: " + task.getException().getMessage());
                    }
                });
            }
        });
    }
}
