package com.example.romvillarica.recipegram;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.*;


public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.login_button)
    Button loginButton;
    @BindView(R.id.register_button)
    Button registerButton;
    @BindView(R.id.username_field)
    EditText usernameField;
    @BindView(R.id.password_field)
    EditText passwordField;

    final int MY_PERMISSIONS_REQUEST_RESULT = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_RESULT) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted!", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(this, "This app will not function properly without granting the appropriate permissions!", Toast.LENGTH_LONG).show();
                requestPermissions();
            }
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
        String userFromPrefs = preferences.getString("username","");
        String passFromPrefs = preferences.getString("password","");

        if (!userFromPrefs.equals("")) {
            usernameField.setText(userFromPrefs);
        }

        if (!passFromPrefs.equals("")) {
            passwordField.setText(passFromPrefs);
        }

        //do permissions stuff
        requestPermissions();

        loginButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final View v1 = v;
                String usernameStr = usernameField.getText().toString();
                String passwordStr = passwordField.getText().toString();
                RedisService.getService().getPost(usernameStr+"-"+passwordStr).enqueue(new Callback<RedisService.GetResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.GetResponse> call, Response<RedisService.GetResponse> response) {
                        UserPost up = response.body().userPost;
                        User u = up.getUser();
                        if (u != null) {
                            SharedPreferences preferences = getSharedPreferences("User", MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("username", u.getUsername());
                            editor.putString("password", u.getPassword());
                            editor.apply();
                            Intent i = new Intent(v1.getContext(),MainActivity.class);
                            i.putExtra("username",u.getUsername());
                            startActivity(i);
                        }
                        else {
                            Toast.makeText(v1.getContext(), "An undefined error occurred.  Try logging in again, or contact Customer Support if the error persists.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RedisService.GetResponse> call, Throwable t) {
                        Toast.makeText(v1.getContext(), "Uh-oh!  This user does not exist, or you may have entered incorrect information.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final View v1 = v;
                String usernameStr = usernameField.getText().toString();
                String passwordStr = passwordField.getText().toString();
                if (usernameStr.length() < 4 || passwordStr.length() < 4) {
                    Toast.makeText(v1.getContext(), "Please enter a username / password over 4 characters in length.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (usernameStr.contains("-") || usernameStr.contains("-")) {
                    Toast.makeText(v1.getContext(), "Please do not use '-' in your username or password.", Toast.LENGTH_LONG).show();
                    return;
                }
                final String userStr = usernameStr;
                final String passStr = passwordStr;
                RedisService.getService().allKeys("*").enqueue(new Callback<RedisService.KeysResponse>() {
                    @Override
                    public void onResponse(Call<RedisService.KeysResponse> call, Response<RedisService.KeysResponse> response) {
                        ArrayList<String> key = response.body().keys;
                        for (int i = 0; i < key.size(); i++) {
                            String[] userPass = key.get(i).split("-");
                            if (userStr.equals(userPass[0])) {
                                Toast.makeText(v1.getContext(), "An account with this username already exists!", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        //otherwise make the new user
                        User u = new User();
                        u.setUsername(userStr);
                        u.setPassword(passStr);
                        UserPost up = new UserPost();
                        up.setUser(u);
                        RedisService.getService().makePost(userStr+"-"+passStr,up).enqueue(new Callback<RedisService.SetResponse>() {
                            @Override
                            public void onResponse(Call<RedisService.SetResponse> call, Response<RedisService.SetResponse> response) {
                                Toast.makeText(v1.getContext(), "Account created!", Toast.LENGTH_LONG).show();
                            }
                            @Override
                            public void onFailure(Call<RedisService.SetResponse> call, Throwable t) {
                                Toast.makeText(v1.getContext(), "Failure to create account :(", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<RedisService.KeysResponse> call, Throwable t) {

                    }
                });

            }
        });
    }

    public void requestPermissions() {
        boolean doIRequest = false;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
           doIRequest = true;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
           doIRequest = true;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            doIRequest = true;
        }

        if (doIRequest) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_RESULT);
        }
    }
}
