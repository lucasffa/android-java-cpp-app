package me.ensine.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class LoginActivity extends Activity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView errorTextView;

    static {
        System.loadLibrary("native-lib");
    }

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

        if (email.isEmpty() || password.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorTextView.setText(R.string.invalid_email_password);
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
            return activity.login(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(String result) {
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (!result.isEmpty()) {
                activity.navigateToDashboard(result);
            } else {
                activity.errorTextView.setText(R.string.login_failed);
            }
        }
    }

    private void navigateToDashboard(String userName) {
        Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
        intent.putExtra("USER_NAME", userName);
        startActivity(intent);
    }

    public native String login(String email, String password);
}
