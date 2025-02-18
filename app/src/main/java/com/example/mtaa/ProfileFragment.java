package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.mtaa.databinding.FragmentProfileBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        loadUserData();
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupToolbar() {
        binding.toolbar.toolbarTitle.setText(R.string.profile);
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        binding.userName.setText(document.getString("name"));
                        binding.userEmail.setText(currentUser.getEmail());
                        
                        // Load profile image if exists
                        String profileImageUrl = document.getString("profileImage");
                        if (profileImageUrl != null) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(binding.profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(getContext(), 
                        "Error loading user data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
        }
    }

    private void setupClickListeners() {
        binding.btnEditProfile.setOnClickListener(v ->
            Navigation.findNavController(v)
                .navigate(R.id.action_profileFragment_to_editProfileFragment));

        binding.btnChangePassword.setOnClickListener(v ->
            Navigation.findNavController(v)
                .navigate(R.id.action_profileFragment_to_changePasswordFragment));

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setPositiveButton(R.string.logout, (dialog, which) -> {
                mAuth.signOut();
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_profileFragment_to_welcomeFragment);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 