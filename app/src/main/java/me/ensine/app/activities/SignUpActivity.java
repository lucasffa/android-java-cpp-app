package me.ensine.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import me.ensine.app.R;
import me.ensine.app.configs.Config;

public class SignUpActivity extends Activity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView errorTextView;

    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView titleText = findViewById(R.id.titleSignUp);
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

        Log.d(TAG, "registerUser: Name: " + name + ", Email: " + email + ", Password: " + password);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorTextView.setText(getString(R.string.signup_error));
            Log.d(TAG, "registerUser: Error - All fields must be filled");
        } else {
            new RegisterTask(this).execute(name, email, password);
        }
    }

    private static class RegisterTask extends AsyncTask<String, Void, Boolean> {
        private final WeakReference<SignUpActivity> activityReference;
        private String responseBody = "";
        private int responseCode = 0;
        private String requestBody = "";

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

            Log.d(TAG, "onPostExecute: Response Code: " + responseCode);
            Log.d(TAG, "onPostExecute: Response Body: " + responseBody);

            if (success) {
                activity.showSuccessDialog();
            } else {
                activity.errorTextView.setText(activity.getString(R.string.signup_error));
                Log.d(TAG, "onPostExecute: SignUp Failed - " + activity.getString(R.string.signup_error));
            }
        }

        private Boolean register(String name, String email, String password) {
            try {
                URL url = new URL(Config.BASE_URL + "/users");
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

                requestBody = gson.toJson(json);
                Log.d(TAG, "register: Request URL: " + url);
                Log.d(TAG, "register: Request Body: " + requestBody);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = requestBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                responseCode = conn.getResponseCode();
                Log.d(TAG, "register: Response Code: " + responseCode);

                StringBuilder response = new StringBuilder();
                BufferedReader br;

                if (responseCode >= 200 && responseCode < 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                }

                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                br.close();

                responseBody = response.toString();
                Log.d(TAG, "register: Response Body: " + responseBody);

                return responseCode == HttpURLConnection.HTTP_CREATED;
            } catch (Exception e) {
                Log.e(TAG, "register: Exception during registration", e);
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
