package com.example.quizzyapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.quizzyapp.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends AndroidViewModel {

    private final MutableLiveData<FirebaseUser> firebaseUserMutableLiveData;
    private final MutableLiveData<Boolean> signInStatus = new MutableLiveData<>();
    private final MutableLiveData<String> signUpStatus = new MutableLiveData<>();
    private final FirebaseUser currentUser;
    private final AuthRepository repository;
    private final FirebaseAuth firebaseAuth;

    public AuthViewModel(@NonNull Application application) {
        super(application);

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize repository and LiveData
        repository = new AuthRepository(application);
        currentUser = repository.getCurrentUser();
        firebaseUserMutableLiveData = repository.getFirebaseUserMutableLiveData();
    }

    // Expose FirebaseUser LiveData
    public LiveData<FirebaseUser> getFirebaseUserMutableLiveData() {
        return firebaseUserMutableLiveData;
    }

    public FirebaseUser getCurrentUser() {
        return currentUser;
    }

    // Expose sign-in status
    public LiveData<Boolean> getSignInStatus() {
        return signInStatus;
    }

    // Expose sign-up status
    public LiveData<String> getSignUpStatus() {
        return signUpStatus;
    }

    // Sign-up method
    public void signUp(String email, String pass, String playerName, String dob, String phoneNumber, String age) {
        repository.signUp(email, pass, playerName, dob, phoneNumber, age, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                firebaseUserMutableLiveData.setValue(repository.getCurrentUser());
            }

            @Override
            public void onFailure(String message) {
                signUpStatus.setValue(message); // Set the error message in LiveData
            }
        });
    }

    // Sign-in method
    public void signIn(String email, String pass) {
        repository.signIn(email, pass, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                signInStatus.setValue(true);
            }

            @Override
            public void onFailure(String message) {
                signInStatus.setValue(false);
            }
        });
    }

    // Sign-out method
    public void signOut() {
        repository.signOut();
    }

    // Reset password method
    public void resetPassword(String email, AuthRepository.AuthCallback callback) {
        if (email == null || email.isEmpty()) {
            callback.onFailure("Email cannot be empty");
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        if (task.getException() != null) {
                            callback.onFailure(task.getException().getMessage());
                        } else {
                            callback.onFailure("Failed to send reset email. Please try again.");
                        }
                    }
                });
    }
}
