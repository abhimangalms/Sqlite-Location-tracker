package com.abhimangalms.trackmylocation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private TextView tvRegister;
    private EditText etLoginGmail,etLoginPassword;
    Button loginButton;

    private SQLiteDatabase db;
    private SQLiteOpenHelper openHelper;
    private Cursor cursor;

    static  final String MY_PREFS_NAME = "LOGIN_PREFERENCE";
    static  final String MY_LOGIN_CREDENTIAL = "IS_LOGGED_N";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        openHelper = new MessageDBHelper(this);
        db = openHelper.getReadableDatabase();
        tvRegister = findViewById(R.id.tvRegister);
        etLoginGmail = findViewById(R.id.etLogGmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        loginButton = findViewById(R.id.buttonLogin);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etLoginGmail.getText().toString().trim();
                String password = etLoginPassword.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Enter your Email and Password to login", Toast.LENGTH_SHORT).show();
                } else {
                    cursor = db.rawQuery("SELECT *FROM " + MessageDBHelper.TABLE_NAME + " WHERE " + MessageDBHelper.COL_4 + "=? AND " + MessageDBHelper.COL_5 + "=?", new String[]{email, password});
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {

                            /** Add shared preference here for login user*/
                            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                            editor.putBoolean(MY_LOGIN_CREDENTIAL, true);
                            editor.apply();

                            Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Login error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });



        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });

    }
}
