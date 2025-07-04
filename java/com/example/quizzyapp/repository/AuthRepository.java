package com.example.quizzyapp.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private final Application application;
    private final MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private final FirebaseAuth firebaseAuth;

    public MutableLiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public AuthRepository(Application application) {
        this.application = application;
        firebaseUserMutableLiveData = new MutableLiveData<>();
        firebaseAuth = FirebaseAuth.getInstance();

        // Check user authentication when the repository is initialized
        if (isAuthenticated()) {
            Log.d("Auth", "Authenticated User: " + firebaseAuth.getCurrentUser().getUid());
        } else {
            Log.d("Auth", "User not authenticated!");
        }
    }

    /**
     * Check if a user is authenticated
     */
    public boolean isAuthenticated() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null;
    }

    /**
     * Sign up a new user with email, password, and additional details
     */

    public void signUp(String email, String pass, String playerName, String dob, String phoneNumber, String age, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                            .setDisplayName(playerName)
                            .build()).addOnCompleteListener(profileTask -> {
                        if (profileTask.isSuccessful()) {
                            saveUserDetails(firebaseUser.getUid(), playerName, dob, phoneNumber, age,0, 1, 0, callback);
                        } else {
                            callback.onFailure("Failed to update profile: " + profileTask.getException().getMessage());
                        }
                    });
                }
            } else {
                callback.onFailure(task.getException().getMessage()); // Pass error message to callback
            }
        });
    }


    /**
     * Save user details to the Firebase Realtime Database
     */
    private void saveUserDetails(String userId, String playerName, String dob, String phoneNumber, String age, int experience, int level, int score, AuthCallback callback) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("playerName", playerName);
        userDetails.put("dob", dob);
        userDetails.put("phoneNumber", phoneNumber);
        userDetails.put("age", age);
        userDetails.put("experience", experience);
        userDetails.put("level", level);
        userDetails.put("score", score);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .setValue(userDetails)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure("Failed to save user details: " + task.getException().getMessage());
                    }
                });
    }



    /**
     * Sign in an existing user with email and password
     */
    public void signIn(String email, String pass, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firebaseUserMutableLiveData.postValue(firebaseAuth.getCurrentUser());
                callback.onSuccess();
            } else {
                callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Sign in failed");
            }
        });
    }

    /**
     * Sign out the currently authenticated user
     */
    public void signOut() {
        firebaseAuth.signOut();
        firebaseUserMutableLiveData.postValue(null);
        Log.d("Auth", "User signed out.");
    }

    // AuthCallback Interface
    public interface AuthCallback {
        void onSuccess();

        void onFailure(String message);
    }
}
