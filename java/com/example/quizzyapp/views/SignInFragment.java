package com.example.quizzyapp.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quizzyapp.R;
import com.example.quizzyapp.repository.AuthRepository;
import com.example.quizzyapp.viewmodel.AuthViewModel;
import com.google.firebase.auth.FirebaseUser;

public class SignInFragment extends Fragment {
    private AuthViewModel viewModel;
    private NavController navController;
    private EditText editEmail, editPass;
    private TextView signUpText, forgotPasswordText;
    private Button signInBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);
        editEmail = view.findViewById(R.id.emailEditSignIN);
        editPass = view.findViewById(R.id.passEditSignIn);
        signUpText = view.findViewById(R.id.signUpText);
        signInBtn = view.findViewById(R.id.signInBtn);
        forgotPasswordText = view.findViewById(R.id.forgotPasswordText);

        // Navigate to the SignUp fragment
        signUpText.setOnClickListener(v -> navController.navigate(R.id.action_signInFragment_to_signUpFragment));

        // Handle "Forgot Password" click
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());

        // Handle Sign-In button click
        signInBtn.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String pass = editPass.getText().toString().trim();

            if (!email.isEmpty() && !pass.isEmpty()) {
                // Sign In and observe status
                viewModel.signIn(email, pass);

                // Observe signInStatus and handle success or failure
                viewModel.getSignInStatus().observe(getViewLifecycleOwner(), isSuccess -> {
                    if (isSuccess) {
                        // Check if the current destination is still SignInFragment before navigating
                        if (navController.getCurrentDestination() != null &&
                                navController.getCurrentDestination().getId() == R.id.signInFragment) {

                            navController.navigate(R.id.action_signInFragment_to_listFragment);
                            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                            // Remove the observer after successful login
                            viewModel.getSignInStatus().removeObservers(getViewLifecycleOwner());
                        }
                    } else {
                        // Handle invalid login attempt
                        Toast.makeText(getContext(), "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please Enter Email and Password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Password");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Enter your email");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                viewModel.resetPassword(email, new AuthRepository.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getContext(), "Password reset email sent successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);
    }
}
