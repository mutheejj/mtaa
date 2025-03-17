package com.example.mtaa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {
    private SharedPreferences preferences;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private MaterialButtonToggleGroup themeToggleGroup;
    private SwitchMaterial pushNotificationsSwitch;
    private SwitchMaterial emailNotificationsSwitch;
    private SwitchMaterial locationSharingSwitch;
    private SwitchMaterial profileVisibilitySwitch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadUserPreferences();
        setupListeners();
    }

    private void initializeViews(View view) {
        themeToggleGroup = view.findViewById(R.id.theme_toggle_group);
        pushNotificationsSwitch = view.findViewById(R.id.push_notifications_switch);
        emailNotificationsSwitch = view.findViewById(R.id.email_notifications_switch);
        locationSharingSwitch = view.findViewById(R.id.location_sharing_switch);
        profileVisibilitySwitch = view.findViewById(R.id.profile_visibility_switch);

        MaterialButton editProfileButton = view.findViewById(R.id.edit_profile_button);
        MaterialButton changePasswordButton = view.findViewById(R.id.change_password_button);

        editProfileButton.setOnClickListener(v -> navigateToEditProfile());
        changePasswordButton.setOnClickListener(v -> navigateToChangePassword());
    }

    private void loadUserPreferences() {
        try {
            String userId = auth.getCurrentUser().getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(document -> {
                        if (document != null && document.exists()) {
                            Boolean pushNotifications = document.getBoolean("pushNotifications");
                            Boolean emailNotifications = document.getBoolean("emailNotifications");
                            Boolean locationSharing = document.getBoolean("locationSharing");
                            Boolean publicProfile = document.getBoolean("publicProfile");

                            pushNotificationsSwitch.setChecked(pushNotifications != null ? pushNotifications : true);
                            emailNotificationsSwitch.setChecked(emailNotifications != null ? emailNotifications : true);
                            locationSharingSwitch.setChecked(locationSharing != null ? locationSharing : true);
                            profileVisibilitySwitch.setChecked(publicProfile != null ? publicProfile : false);
                        }
                    })
                    .addOnFailureListener(e -> showError("Failed to load preferences"));

            // Load theme preference
            int currentTheme = preferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            int buttonId = R.id.theme_system;
            if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
                buttonId = R.id.theme_light;
            } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
                buttonId = R.id.theme_dark;
            }
            themeToggleGroup.check(buttonId);
        } catch (Exception e) {
            showError("Error loading preferences");
        }
    }

    private void setupListeners() {
        themeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int themeMode;
                if (checkedId == R.id.theme_light) {
                    themeMode = AppCompatDelegate.MODE_NIGHT_NO;
                } else if (checkedId == R.id.theme_dark) {
                    themeMode = AppCompatDelegate.MODE_NIGHT_YES;
                } else {
                    themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }
                preferences.edit().putInt("theme_mode", themeMode).apply();
                AppCompatDelegate.setDefaultNightMode(themeMode);
            }
        });

        setupPreferenceChangeListener(pushNotificationsSwitch, "pushNotifications");
        setupPreferenceChangeListener(emailNotificationsSwitch, "emailNotifications");
        setupPreferenceChangeListener(locationSharingSwitch, "locationSharing");
        setupPreferenceChangeListener(profileVisibilitySwitch, "publicProfile");
    }

    private void setupPreferenceChangeListener(SwitchMaterial switchView, String preferenceName) {
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try {
                String userId = auth.getCurrentUser().getUid();
                db.collection("users").document(userId)
                        .update(preferenceName, isChecked)
                        .addOnFailureListener(e -> {
                            showError("Failed to update " + preferenceName);
                            switchView.setChecked(!isChecked);
                        });
            } catch (Exception e) {
                showError("Error updating preference");
                switchView.setChecked(!isChecked);
            }
        });
    }

    private void navigateToEditProfile() {
        try {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            showError("Error navigating to Edit Profile");
        }
    }

    private void navigateToChangePassword() {
        try {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new ChangePasswordFragment())
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            showError("Error navigating to Change Password");
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}