package me.ensine.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignUpActivity extends Activity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameEditText = findViewById(R.id.editTextName);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        errorTextView = findViewById(R.id.textViewError);
        Button registerButton = findViewById(R.id.buttonRegister);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorTextView.setText(getString(R.string.signup_error));
        } else {
            new RegisterTask(this).execute(name, email, password);
        }
    }

    private static class RegisterTask extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<SignUpActivity> activityReference;

        RegisterTask(SignUpActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            SignUpActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return false;
            return register(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            SignUpActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (success) {
                activity.showSuccessDialog();
            } else {
                activity.errorTextView.setText(activity.getString(R.string.signup_error));
            }
        }

        private static Boolean register(String name, String email, String password) {
            try {
                URL url = new URL(Config.BASE_URL + "/users/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                Gson gson = new Gson();
                JsonObject json = new JsonObject();
                json.addProperty("name", name);
                json.addProperty("email", email);
                json.addProperty("password", password);

                String jsonInputString = gson.toJson(json);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();

                if (code == HttpURLConnection.HTTP_CREATED) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        return true;
                    }
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.signup_success))
                .setPositiveButton(getString(R.string.next_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateToMain();
                    }
                })
                .show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
