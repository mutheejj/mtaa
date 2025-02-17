package com.example.mtaa;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.example.mtaa.databinding.FragmentSignInBinding;

public class SignInFragment extends Fragment {
    private FragmentSignInBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        // Initialize view binding
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set click listeners
        binding.btnSignIn.setOnClickListener(v -> signIn());
        
        binding.btnBack.setOnClickListener(v -> 
            Navigation.findNavController(view).navigateUp());
        
        // Remove the forgot password navigation for now
        binding.forgotPasswordLink.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Feature coming soon", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void signIn() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validate inputs
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            binding.etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Password should be at least 6 characters");
            binding.etPassword.requestFocus();
            return;
        }

        // Show loading state
        binding.btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                binding.btnSignIn.setEnabled(true);
                if (task.isSuccessful()) {
                    // Add logging to debug navigation
                    Log.d("SignInFragment", "Sign in successful, attempting navigation");
                    try {
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_signInFragment_to_homeFragment);
                    } catch (Exception e) {
                        Log.e("SignInFragment", "Navigation failed", e);
                        Toast.makeText(requireContext(), 
                            "Navigation error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = task.getException() != null ? 
                        task.getException().getMessage() : 
                        "Authentication failed";
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 