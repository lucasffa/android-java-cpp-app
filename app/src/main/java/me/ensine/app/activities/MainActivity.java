package me.ensine.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import me.ensine.app.R;
import me.ensine.app.managers.SharedPreferencesManager;
import me.ensine.app.entities.User;
import me.ensine.app.repositories.UserRepository;
import me.ensine.app.configs.Config;

public class MainActivity extends Activity {

    private Button loginButton;
    private Button signUpButton;
    private ProgressBar progressBar;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = findViewById(R.id.buttonLogin);
        signUpButton = findViewById(R.id.buttonSignUp);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUp();
            }
        });

        // Verifica se há dados no SQLite e inicia a verificação do token
        new VerifyUserTask(this).execute();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    private static class VerifyUserTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<MainActivity> activityReference;
        private String uuid;
        private String token;

        VerifyUserTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            Log.d(TAG, "onPreExecute: Exibindo progress bar");
            activity.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                Log.d(TAG, "doInBackground: Atividade é nula ou está terminando");
                return false;
            }

            Log.d(TAG, "doInBackground: Recuperando dados do usuário do SQLite");
            User user = UserRepository.getInstance(activity).getUserDetails();
            if (user == null) {
                Log.d(TAG, "doInBackground: Nenhum usuário encontrado no SQLite");
                return false;
            }

            token = user.getToken();
            uuid = user.getUuid();
            String lastLogin = user.getLastLogin();

            Log.d(TAG, "doInBackground: Dados do usuário recuperados do SQLite - UUID: " + uuid + ", Token: " + token + ", Last Login: " + lastLogin);

            long currentTime = new Date().getTime();
            long lastLoginTime;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date lastLoginDate = sdf.parse(lastLogin);

                // Convertendo para o fuso horário local
                SimpleDateFormat localSdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                localSdf.setTimeZone(TimeZone.getDefault());
                lastLoginTime = localSdf.parse(localSdf.format(lastLoginDate)).getTime();
            } catch (ParseException e) {
                Log.e(TAG, "doInBackground: Erro ao parsear data do último login", e);
                return false;
            }

            long timeDiff = (currentTime - lastLoginTime) / (1000 * 60);
            Log.d(TAG, "doInBackground: Tempo desde o último login: " + timeDiff + " minutos");
            if (timeDiff > 50) {
                Log.d(TAG, "doInBackground: Último login foi há mais de 50 minutos");
                return false;
            }

            return verifyUser(uuid);
        }

        private boolean verifyUser(String uuid) {
            try {
                URL url = new URL(Config.BASE_URL + "/users/uuid?uuid=" + uuid);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                int code = conn.getResponseCode();
                Log.d(TAG, "verifyUser: Código de resposta HTTP: " + code);

                StringBuilder response = new StringBuilder();
                BufferedReader br;

                if (code >= 200 && code < 300) {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"));
                }

                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                br.close();
                Log.d(TAG, "verifyUser: Corpo da resposta HTTP: " + response.toString());

                if (code == HttpsURLConnection.HTTP_OK) {
                    JSONObject user = new JSONObject(response.toString());

                    String name = user.getString("name");
                    String email = user.getString("email");
                    String lastLoginAt = user.getString("lastLoginAt");
                    String role = user.optString("role", "");

                    Log.d(TAG, "verifyUser: Detalhes do usuário - Nome: " + name + ", Email: " + email + ", Último login: " + lastLoginAt);

                    UserRepository.getInstance(activityReference.get()).saveUserDetails(token, name, lastLoginAt, uuid, role);
                    SharedPreferencesManager.getInstance(activityReference.get()).saveUserDetails(token, name, lastLoginAt, uuid, role);

                    return true;
                } else {
                    Log.e(TAG, "verifyUser: Erro na requisição HTTP, código: " + code);
                    return false;
                }
            } catch (Exception e) {
                Log.e(TAG, "verifyUser: Exceção durante a verificação do usuário", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            Log.d(TAG, "onPostExecute: Ocultando progress bar");
            activity.progressBar.setVisibility(View.GONE);
            if (result) {
                Log.d(TAG, "onPostExecute: Verificação bem-sucedida, navegando para DashboardActivity");
                Intent intent = new Intent(activity, DashboardActivity.class);
                activity.startActivity(intent);
            } else {
                Log.d(TAG, "onPostExecute: Verificação falhou, solicitando novo login");
                Toast.makeText(activity, "Favor refazer login.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
