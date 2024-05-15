package me.ensine.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class DashboardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        String userName = getIntent().getStringExtra("USER_NAME");
        TextView userNameTextView = findViewById(R.id.textViewUserName);
        userNameTextView.setText("Welcome, " + userName);
    }
}
