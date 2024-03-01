package com.sobhy.notesapplication.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.sobhy.notesapplication.R;

public class CreateAccountFragment extends Fragment {
    TextInputEditText emailTextInput, passwordTextInput, confirmPasswordTextInput;
    AppCompatButton signUpBtn;
    TextView loginTv;
    ProgressBar progressBar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_account, container, false);
        emailTextInput = view.findViewById(R.id.signup_textInputEditText_email);
        passwordTextInput = view.findViewById(R.id.signup_textInputEditText_password);
        confirmPasswordTextInput = view.findViewById(R.id.signup_textInputEditText_confirm_password);
        signUpBtn = view.findViewById(R.id.signup_btn_signup);
        loginTv = view.findViewById(R.id.signup_tv_login);
        progressBar = view.findViewById(R.id.signup_progress_bar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailTextInput.setOnFocusChangeListener((view1, hasFocus) -> {
            if (!hasFocus ){
                if (!Patterns.EMAIL_ADDRESS.matcher(emailTextInput.getText().toString()).matches()){
                    emailTextInput.setError(getString(R.string.email_error));
                }
            }
        });
        loginTv.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_createAccountFragment_to_loginFragment);
        });
        signUpBtn.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String email= emailTextInput.getText().toString();
        String password= passwordTextInput.getText().toString();
        String confirmPassword= confirmPasswordTextInput.getText().toString();

        if (!validateData(email, password, confirmPassword)){
            return;
        }
        createAccountInFirebase(email, password);
    }

    private void createAccountInFirebase(String email, String password) {
        changeInProgress(true);

        FirebaseAuth firebaseAuth= FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), task -> {
            changeInProgress(false);
            if(task.isSuccessful()){
                Toast.makeText(requireContext(), R.string.created_account_successfully, Toast.LENGTH_SHORT).show();
                firebaseAuth.getCurrentUser().sendEmailVerification();
                firebaseAuth.signOut();
            } else {
                Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeInProgress(boolean b) {
        if(b){
            progressBar.setVisibility(View.VISIBLE);
            signUpBtn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            signUpBtn.setEnabled(true);
        }
    }

    private boolean validateData(String email, String password, String confirmPassword){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailTextInput.setError(getString(R.string.email_error));
            return false;
        }
        if(password.length()<8){
            passwordTextInput.setError(getString(R.string.password_error));
            return false;
        }
        if (!confirmPassword.equals(password)) {
            confirmPasswordTextInput.setError(getString(R.string.not_same_password));
            return false;
        }
        return true;
    }
}