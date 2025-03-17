package com.example.mtaa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mtaa.databinding.FragmentEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    selectedImageUri = data.getData();
                    Glide.with(this)
                        .load(selectedImageUri)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(binding.profileImage);
                }
            }
        });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        binding.toolbar.toolbarTitle.setText(R.string.edit_profile);
        binding.toolbar.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        loadUserProfile();

        binding.btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });

        binding.btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadUserProfile() {
        if (binding == null) return;
        
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            
            db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (binding == null) return;
                    if (document.exists()) {
                        String name = document.getString("name");
                        String profileImageUrl = document.getString("profileImage");
                        
                        if (name != null) {
                            binding.etDisplayName.setText(name);
                        }
                        
                        if (profileImageUrl != null && !profileImageUrl.isEmpty() && isAdded()) {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(binding.profileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), 
                    "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void saveChanges() {
        String displayName = binding.etDisplayName.getText().toString().trim();
        
        if (TextUtils.isEmpty(displayName)) {
            binding.etDisplayName.setError("Display name is required");
            binding.etDisplayName.requestFocus();
            return;
        }

        binding.btnSave.setEnabled(false);
        FirebaseUser user = mAuth.getCurrentUser();
        
        if (user != null) {
            String uid = user.getUid();
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", displayName);

            if (selectedImageUri != null) {
                StorageReference profileRef = storage.getReference()
                    .child("profile_images")
                    .child(uid + ".jpg");

                profileRef.putFile(selectedImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return profileRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            updates.put("profileImage", downloadUri.toString());
                            updateProfile(updates);
                        } else {
                            binding.btnSave.setEnabled(true);
                            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                updateProfile(updates);
            }
        }
    }

    private void updateProfile(Map<String, Object> updates) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(getView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}