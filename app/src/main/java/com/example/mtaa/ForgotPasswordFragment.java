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

import com.example.mtaa.databinding.FragmentForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {
    private FragmentForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Setup back button
        binding.btnBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());

        // Setup reset password button
        binding.btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.emailInput.getText().toString().trim();

        // Enhanced email validation
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (email.isEmpty()) {
            binding.emailInput.setError("Email is required");
            binding.emailInput.requestFocus();
            return;
        } else if (!email.matches(emailPattern)) {
            binding.emailInput.setError("Please enter a valid email address");
            binding.emailInput.requestFocus();
            return;
        }

        // Show loading state
        binding.btnResetPassword.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        // Send password reset email with enhanced error handling
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    binding.btnResetPassword.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(),
                                "Password reset email sent. Please check your email (including spam folder).",
                                Toast.LENGTH_LONG).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    } else {
                        String errorMessage;
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null && exceptionMessage.contains("no user record")) {
                                errorMessage = "No account found with this email address";
                            } else if (exceptionMessage != null && exceptionMessage.contains("badly formatted")) {
                                errorMessage = "Invalid email format";
                            } else {
                                errorMessage = "Failed to send reset email: " + exceptionMessage;
                            }
                        } else {
                            errorMessage = "Failed to send reset email. Please try again later";
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}