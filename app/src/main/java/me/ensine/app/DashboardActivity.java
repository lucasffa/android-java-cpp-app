package me.ensine.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DashboardActivity extends Activity {

    private boolean isTokenVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferencesManager prefs = SharedPreferencesManager.getInstance(this);

        String userName = prefs.getName();
        String lastLogin = prefs.getLastLogin();
        final String token = prefs.getToken();
        String uuid = prefs.getUuid();
        String role = prefs.getRole();

        TextView userNameTextView = findViewById(R.id.textViewUserName);
        TextView lastLoginTextView = findViewById(R.id.textViewLastLogin);
        final TextView tokenTextView = findViewById(R.id.textViewToken);
        TextView uuidTextView = findViewById(R.id.textViewUuid);
        TextView roleTextView = findViewById(R.id.textViewRole);
        final Button tokenButton = findViewById(R.id.buttonShowToken);
        Button logoutButton = findViewById(R.id.buttonLogout);

        String welcomeMessage = getString(R.string.welcome_message, userName);
        String lastLoginMessage = getString(R.string.last_login_message, formatDateTime(lastLogin));
        String showTokenText = getString(R.string.show_token);
        String hideTokenText = getString(R.string.hide_token);

        userNameTextView.setText(welcomeMessage);
        lastLoginTextView.setText(lastLoginMessage);
        uuidTextView.setText(getString(R.string.uuid_message, uuid));
        roleTextView.setText(getString(R.string.role_message, role));
        tokenButton.setText(showTokenText);

        tokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTokenVisible) {
                    tokenTextView.setText("");
                    tokenButton.setText(showTokenText);
                    isTokenVisible = false;
                } else {
                    tokenTextView.setText("Token: " + token);
                    tokenButton.setText(hideTokenText);
                    isTokenVisible = true;
                }
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
    }

    private String formatDateTime(String dateTime) {
        SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat localFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
        localFormat.setTimeZone(TimeZone.getDefault());

        try {
            Date date = utcFormat.parse(dateTime);
            return localFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateTime;
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing to disable back button
    }

    private void logOut() {
        // Clear shared preferences
        SharedPreferencesManager.getInstance(this).clear();

        // Mark the user as logged off in the database
        UserRepository.getInstance(this).logOffUser();

        // Navigate back to MainActivity
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
