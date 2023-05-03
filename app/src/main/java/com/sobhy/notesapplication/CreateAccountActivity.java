package com.sobhy.notesapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class CreateAccountActivity extends AppCompatActivity {
    TextInputEditText emailTextInput, passwordTextInput, confirmPasswordTextInput;
    AppCompatButton signUpBtn;
    TextView loginTv;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        emailTextInput= findViewById(R.id.signup_textInputEditText_email);
        emailTextInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus ){
                if (!Patterns.EMAIL_ADDRESS.matcher(emailTextInput.getText().toString()).matches()){
                    emailTextInput.setError(getString(R.string.email_error));
                }
            }
        });
        passwordTextInput= findViewById(R.id.signup_textInputEditText_password);
        confirmPasswordTextInput= findViewById(R.id.signup_textInputEditText_confirm_password);
        signUpBtn= findViewById(R.id.signup_btn_signup);
        loginTv= findViewById(R.id.signup_tv_login);
        progressBar= findViewById(R.id.signup_progress_bar);
        loginTv.setOnClickListener(v -> {
            startActivity(new Intent(getBaseContext(), LoginActivity.class));
            finish();
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
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            changeInProgress(false);
            if(task.isSuccessful()){
                Toast.makeText(this, R.string.created_account_successfully, Toast.LENGTH_SHORT).show();
                firebaseAuth.getCurrentUser().sendEmailVerification();
                firebaseAuth.signOut();
                finish();
            } else {
                Toast.makeText(this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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