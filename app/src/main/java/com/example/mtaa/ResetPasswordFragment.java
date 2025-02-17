package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageButton;

public class ResetPasswordFragment extends Fragment {
    private FirebaseAuth mAuth;
    private TextInputEditText emailInput;
    private MaterialButton resetButton;
    private ImageButton backButton;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize views with correct IDs
        emailInput = view.findViewById(R.id.etEmail);
        resetButton = view.findViewById(R.id.btn_reset);
        backButton = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progress_bar);

        // Set click listeners
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        resetButton.setOnClickListener(v -> {
            resetPassword();
        });

        return view;
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        // Validate email
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return;
        }

        // Show loading state
        progressBar.setVisibility(View.VISIBLE);
        resetButton.setEnabled(false);

        // Send reset email
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                // Hide loading state
                progressBar.setVisibility(View.GONE);
                resetButton.setEnabled(true);

                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                    
                    // Navigate back
                    Navigation.findNavController(getView()).navigateUp();
                } else {
                    Toast.makeText(getContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show();
                }
            });
    }
} 