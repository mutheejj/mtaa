package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mtaa.databinding.FragmentSignInBinding;

public class SignInFragment extends Fragment {
    private FragmentSignInBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater, container, false);
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

        if (email.isEmpty()) {
            binding.emailLayout.setError("Email is required");
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        if (password.isEmpty()) {
            binding.passwordLayout.setError("Password is required");
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        return isValid;
    }

    private void signIn(String email, String password) {
        // Implement your sign-in logic here
        // For example, using Firebase Authentication
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 