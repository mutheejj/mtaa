package com.example.mtaa;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mtaa.databinding.FragmentChangePasswordBinding;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        binding.toolbar.toolbarTitle.setText(R.string.change_password);
        binding.toolbar.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        if (binding == null || !isAdded()) return;

        String currentPassword = binding.currentPassword.getText().toString().trim();
        String newPassword = binding.newPassword.getText().toString().trim();
        String confirmNewPassword = binding.confirmNewPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            binding.currentPassword.setError("Current password is required");
            binding.currentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            binding.newPassword.setError("New password is required");
            binding.newPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmNewPassword)) {
            binding.confirmNewPassword.setError("Please confirm new password");
            binding.confirmNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            binding.confirmNewPassword.setError("Passwords do not match");
            binding.confirmNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            binding.newPassword.setError("Password must be at least 6 characters");
            binding.newPassword.requestFocus();
            return;
        }

        // Disable button to prevent multiple clicks
        binding.btnChangePassword.setEnabled(false);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Re-authenticate user before changing password
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener(updateTask -> {
                                binding.btnChangePassword.setEnabled(true);
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(getView()).navigateUp();
                                } else {
                                    Toast.makeText(getContext(), "Failed to update password: " + 
                                        updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                    } else {
                        binding.btnChangePassword.setEnabled(true);
                        binding.currentPassword.setError("Current password is incorrect");
                        binding.currentPassword.requestFocus();
                    }
                });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}