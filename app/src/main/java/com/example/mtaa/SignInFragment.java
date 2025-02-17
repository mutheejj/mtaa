package com.example.mtaa;

import android.os.Bundle;
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
    private FirebaseAuth mAuth;
    private FragmentSignInBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        // Initialize binding
        binding = FragmentSignInBinding.inflate(inflater, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        
        // Use binding to access views
        binding.btnSignIn.setOnClickListener(v -> signIn());
        
        return binding.getRoot();
    }

    private void signIn() {
        // Use binding to access EditText values
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

        // Show loading state (you might want to add a progress bar)
        binding.btnSignIn.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                binding.btnSignIn.setEnabled(true);
                if (task.isSuccessful()) {
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.action_signInFragment_to_homeFragment);
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