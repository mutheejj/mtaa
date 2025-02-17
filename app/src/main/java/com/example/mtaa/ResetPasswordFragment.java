package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordFragment extends Fragment {
    private FirebaseAuth mAuth;
    private EditText emailInput;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        
        emailInput = view.findViewById(R.id.email_input);
        Button resetButton = view.findViewById(R.id.btn_reset_password);
        Button backButton = view.findViewById(R.id.btn_back);

        resetButton.setOnClickListener(v -> resetPassword());
        
        backButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), 
                        "Password reset link sent to your email", 
                        Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), 
                        "Failed to send reset email: " + task.getException().getMessage(), 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
} 