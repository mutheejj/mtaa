package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mtaa.databinding.FragmentCreateAccountBinding;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountFragment extends Fragment {
    private FragmentCreateAccountBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentCreateAccountBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        // Setup back button
        binding.btnBack.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp());

        // Setup create account button
        binding.btnCreateAccount.setOnClickListener(v -> createAccount());

        return binding.getRoot();
    }

    private void createAccount() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        String name = binding.nameInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Account created successfully
                    Toast.makeText(getContext(), "Account created successfully", 
                        Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot())
                        .navigate(R.id.action_createAccountFragment_to_homeFragment);
                } else {
                    // If sign up fails, display a message to the user.
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