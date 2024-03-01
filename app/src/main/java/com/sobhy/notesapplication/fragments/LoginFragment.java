package com.sobhy.notesapplication.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sobhy.notesapplication.R;
public class LoginFragment extends Fragment {
    TextInputEditText emailEditText, passwordEditText;
    AppCompatButton loginBtn;
    TextView forgotPassword, createAccountTv;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    Dialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        emailEditText = view.findViewById(R.id.login_textInputEditText_email);
        passwordEditText = view.findViewById(R.id.login_textInputEditText_password);
        loginBtn= view.findViewById(R.id.login_btn_login);
        forgotPassword = view.findViewById(R.id.login_tv_forgot_password);
        createAccountTv = view.findViewById(R.id.login_tv_createaccount);
        progressBar = view.findViewById(R.id.login_progress_bar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build();
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_notesFragment, savedInstanceState, navOptions);
        }

        emailEditText.setOnFocusChangeListener((view1, hasFocus) -> {
            if (!hasFocus ){
                if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()){
                    emailEditText.setError(getString(R.string.email_error));
                }
            }
        });
        forgotPassword.setOnClickListener(v -> showResetDialog());
        createAccountTv.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_createAccountFragment)

        );
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void showResetDialog() {
        dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.custom_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        EditText emailReset = dialog.findViewById(R.id.dialog_et_email);
        AppCompatButton resetBtn = dialog.findViewById(R.id.dialog_btn_reset);
        dialog.show();

        resetBtn.setOnClickListener(v -> {
            String emailInDialog = emailReset.getText().toString();
            if (Patterns.EMAIL_ADDRESS.matcher(emailInDialog).matches()) {
                resetPassword(emailInDialog);
            } else {
                emailReset.setError(getString(R.string.email_error));
            }

        });
    }

    private void resetPassword(String email) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), R.string.email_sent, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (!validateData(email, password)) {
            return;
        }
        loginAccountInFirebase(email, password);
    }

    private void loginAccountInFirebase(String email, String password) {
        changeInProgress(true);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            changeInProgress(false);
            if (task.isSuccessful()) {
                if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_notesFragment);
                } else {
                    Toast.makeText(requireContext(), R.string.email_should_verified, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeInProgress(boolean b) {
        if (b) {
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginBtn.setEnabled(true);
        }
    }

    private boolean validateData(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.email_error));
            return false;
        }
        if (password.length() < 8) {
            passwordEditText.setError(getString(R.string.password_error));
            return false;
        }
        return true;
    }
}