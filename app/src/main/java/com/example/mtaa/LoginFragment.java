package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.mtaa.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        
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
            String email = binding.emailInput.getText().toString();
            String password = binding.passwordInput.getText().toString();
            
            // TODO: Implement login logic
            // For now, just navigate to home
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_homeFragment);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 