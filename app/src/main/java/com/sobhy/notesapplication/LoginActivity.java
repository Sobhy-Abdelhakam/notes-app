package com.sobhy.notesapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText emailEditText, passwordEditText;
    AppCompatButton loginBtn;
    TextView forgotPassword, createAccountTv;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.login_textInputEditText_email);
        emailEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus ){
                if (!Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()){
                    emailEditText.setError(getString(R.string.email_error));
                }
            }
        });
        passwordEditText = findViewById(R.id.login_textInputEditText_password);
        loginBtn= findViewById(R.id.login_btn_login);
        forgotPassword = findViewById(R.id.login_tv_forgot_password);
        createAccountTv = findViewById(R.id.login_tv_createaccount);
        progressBar = findViewById(R.id.login_progress_bar);
        forgotPassword.setOnClickListener(v -> showResetDialog());
        createAccountTv.setOnClickListener(v -> startActivity(new Intent(getBaseContext(), CreateAccountActivity.class)));
        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void showResetDialog() {
        dialog = new Dialog(this);
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
                Toast.makeText(this, R.string.email_sent, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, R.string.email_should_verified, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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