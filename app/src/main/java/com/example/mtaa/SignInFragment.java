package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mtaa.databinding.FragmentSignInBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {
    private FragmentSignInBinding binding;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigate to Sign Up
        binding.signUpLink.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_signInFragment_to_signUpFragment)
        );

        // Navigate to Forgot Password
        binding.forgotPasswordLink.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_signInFragment_to_forgotPasswordFragment)
        );

        // Handle Sign In
        binding.signInButton.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();

            if (validateInput(email, password)) {
                signIn(email, password);
            }
        });
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (email.isEmpty()) {
            binding.emailLayout.setError("Email is required");
            isValid = false;
        } else if (!email.matches(emailPattern)) {
            binding.emailLayout.setError("Please enter a valid email address");
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            binding.passwordLayout.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            binding.passwordLayout.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        return isValid;
    }

    private void signIn(String email, String password) {
        if (!validateInput(email, password)) {
            return;
        }
        
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Navigate to Home screen
                    Navigation.findNavController(getView())
                        .navigate(R.id.action_signInFragment_to_homeFragment);
                } else {
                    // Show error message
                    Toast.makeText(getContext(), "Authentication failed: " + 
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}