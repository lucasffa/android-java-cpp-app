package me.ensine.app;

import android.app.Activity;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends Activity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView errorTextView;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        errorTextView = findViewById(R.id.textViewError);
        Button loginButton = findViewById(R.id.buttonLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });
    }

    private void validateAndLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        Log.d(TAG, "Validating email: " + email);
        Log.d(TAG, "Validating password: " + password);

        if (email.isEmpty() || password.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorTextView.setText(R.string.invalid_email_password);
            Log.e(TAG, "Invalid email or password format.");
        } else if (password.length() < 8) {
            errorTextView.setText(R.string.invalid_password_length);
            Log.e(TAG, "Password is too short.");
        } else {
            new LoginTask(this).execute(email, password);
        }
    }

    private static class LoginTask extends AsyncTask<String, Void, String> {
        private final WeakReference<LoginActivity> activityReference;

        LoginTask(LoginActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(String... params) {
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return "";
            return login(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            try {
                JSONObject jsonResponse = new JSONObject(result);
                JSONObject user = jsonResponse.getJSONObject("user");
                String token = jsonResponse.getString("token");
                String name = user.getString("name");
                String lastLogin = user.getString("lastLoginAt");
                String uuid = user.getString("uuid");
                String role = user.getString("role");

                // Save user details in SharedPreferences
                SharedPreferencesManager.getInstance(activity).saveUserDetails(token, name, lastLogin, uuid, role);

                // Save user details in SQLite
                UserRepository.getInstance(activity).saveUserDetails(token, name, lastLogin, uuid, role);

                Log.d(TAG, "User details saved: " + name);

                // Pass user details to DashboardActivity
                activity.navigateToDashboard(token, name, lastLogin, uuid, role);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing login response", e);
                activity.errorTextView.setText(R.string.login_failed);
            }
        }

        private static String login(String email, String password) {
            try {
                URL url = new URL(Config.BASE_URL + "/users/login");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                // Usando Gson para criar o JSON
                Gson gson = new Gson();
                JsonObject json = new JsonObject();
                json.addProperty("email", email);
                json.addProperty("password", password);

                String jsonInputString = gson.toJson(json);
                Log.d(TAG, "Request URL: " + url);
                Log.d(TAG, "Request Method: " + conn.getRequestMethod());
                Log.d(TAG, "Request Headers: Content-Type=application/json; utf-8, Accept=application/json");
                Log.d(TAG, "Request Body: " + jsonInputString);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + code);

                if (code == HttpsURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d(TAG, "Response Body: " + response.toString());
                        return response.toString();
                    }
                } else {
                    Log.e(TAG, "HTTP Error: " + code);
                    // Adicionando mais detalhes do erro
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String errorLine;
                        while ((errorLine = br.readLine()) != null) {
                            errorResponse.append(errorLine.trim());
                        }
                        Log.e(TAG, "Error Response Body: " + errorResponse.toString());
                    }
                    return "";
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: ", e);
                return "";
            }
        }
    }

    private void navigateToDashboard(String token, String name, String lastLogin, String uuid, String role) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("TOKEN", token);
        intent.putExtra("USER_NAME", name);
        intent.putExtra("LAST_LOGIN", lastLogin);
        intent.putExtra("UUID", uuid);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
