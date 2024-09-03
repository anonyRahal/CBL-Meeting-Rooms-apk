package com.example.cbl_teams_rooms.activity;
import com.example.cbl_teams_rooms.R;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import android.content.SharedPreferences;


import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Objects.requireNonNull(getWindow().getDecorView().getWindowInsetsController()).hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        }


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        usernameEditText = findViewById(R.id.user_name); // Assuming you have username EditText
        passwordEditText = findViewById(R.id.password); // Assuming you have password EditText

        Button loginButton = findViewById(R.id.login_button); // Assuming you have a login button

        loginButton.setOnClickListener(v -> validateCredentials());


    }

    private void validateCredentials() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if ("admin".equals(username) && "pass".equals(password)) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Handle incorrect credentials (e.g., show an error message)
        }
    }


}
