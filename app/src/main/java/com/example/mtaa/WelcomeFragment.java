package com.example.mtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class WelcomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        view.findViewById(R.id.btnLogin).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_welcomeFragment_to_loginFragment));

        view.findViewById(R.id.btnCreateAccount).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_welcomeFragment_to_createAccountFragment));

        view.findViewById(R.id.btnOfficials).setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_welcomeFragment_to_adminLoginFragment));

        return view;
    }
}