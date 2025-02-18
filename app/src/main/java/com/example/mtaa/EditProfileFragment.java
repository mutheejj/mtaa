package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mtaa.databinding.FragmentEditProfileBinding;

public class EditProfileFragment extends Fragment {
    private FragmentEditProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.toolbar.toolbarTitle.setText(R.string.edit_profile);
        // Add your implementation here
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 