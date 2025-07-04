package com.example.quizzyapp.views;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.quizzyapp.R;
import com.example.quizzyapp.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class SignUpFragment extends Fragment {

    private AuthViewModel viewModel;
    private NavController navController;

    private EditText editEmail, editPass, editPlayerName, editDOB, editPhoneNumber, editAge;
    private TextView signInText;
    private Button signUpBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // Initialize UI elements
        editPlayerName = view.findViewById(R.id.editPlayerName);
        editEmail = view.findViewById(R.id.editEmailSignUp);
        editPass = view.findViewById(R.id.editPassSignUp);
        editDOB = view.findViewById(R.id.editDOB);
        editPhoneNumber = view.findViewById(R.id.editPhoneNumber);
        editAge = view.findViewById(R.id.editAge);
        signInText = view.findViewById(R.id.signInText);
        signUpBtn = view.findViewById(R.id.signUpBtn);

        // Navigate to Sign-In screen
        signInText.setOnClickListener(v -> navController.navigate(R.id.action_signUpFragment_to_signInFragment));

        // Handle Sign-Up button click
        signUpBtn.setOnClickListener(v -> {
            String playerName = editPlayerName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String pass = editPass.getText().toString().trim();
            String dob = editDOB.getText().toString().trim();
            String phoneNumber = editPhoneNumber.getText().toString().trim();
            String age = editAge.getText().toString().trim();

            if (validateInput(playerName, email, pass, dob, phoneNumber, age)) {
                viewModel.signUp(email, pass, playerName, dob, phoneNumber, age);

                // Observe FirebaseUser updates
                viewModel.getFirebaseUserMutableLiveData().observe(getViewLifecycleOwner(), firebaseUser -> {
                    if (firebaseUser != null) {
                        // Check current destination before navigating
                        if (navController.getCurrentDestination() != null &&
                                navController.getCurrentDestination().getId() == R.id.signUpFragment) {

                            Toast.makeText(getContext(), "Registered Successfully", Toast.LENGTH_SHORT).show();
                            navController.navigate(R.id.action_signUpFragment_to_signInFragment);

                            // Remove observers after successful navigation
                            viewModel.getFirebaseUserMutableLiveData().removeObservers(getViewLifecycleOwner());
                        }
                    }
                });

                // Observe errors
                viewModel.getSignUpStatus().observe(getViewLifecycleOwner(), message -> {
                    if (message != null) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

                        // Optionally remove the observer after showing the message
                        viewModel.getSignUpStatus().removeObservers(getViewLifecycleOwner());
                    }
                });
            }
        });


    }

    private boolean validateInput(String playerName, String email, String pass, String dob, String phoneNumber, String age) {
        if (playerName.isEmpty() || email.isEmpty() || pass.isEmpty() || dob.isEmpty() || phoneNumber.isEmpty() || age.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pass.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(getContext(), "Invalid DOB format. Use DD/MM/YYYY", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!phoneNumber.matches("\\d+")) {
            Toast.makeText(getContext(), "Phone number must contain only digits", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int parsedAge = Integer.parseInt(age);
            if (parsedAge <= 0) {
                Toast.makeText(getContext(), "Age must be a positive number", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Age must be a valid number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);
    }
}
