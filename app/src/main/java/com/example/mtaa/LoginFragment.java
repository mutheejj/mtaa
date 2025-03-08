package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mtaa.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        
        // Setup back button
        binding.btnBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());

        // Setup forgot password link
        binding.forgotPasswordLink.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_resetPasswordFragment));

        // Setup create account link
        binding.createAccountLink.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_createAccountFragment));

        // Setup login button
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.emailInput.getText().toString().trim();
            String password = binding.passwordInput.getText().toString().trim();
            
            // Validate input fields
            if (email.isEmpty()) {
                binding.emailInput.setError("Email is required");
                binding.emailInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.passwordInput.setError("Password is required");
                binding.passwordInput.requestFocus();
                return;
            }

            // Show loading state
            binding.btnLogin.setEnabled(false);
            
            // Authenticate with Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.btnLogin.setEnabled(true);
                    
                    if (task.isSuccessful()) {
                        // Login successful, navigate to home
                        Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_homeFragment);
                    } else {
                        // Login failed
                        String errorMessage = task.getException() != null ? 
                            task.getException().getMessage() : 
                            "Authentication failed";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}