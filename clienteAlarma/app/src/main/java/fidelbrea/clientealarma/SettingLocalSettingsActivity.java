/*
 * Copyright (C) 2022 Fidel Brea Montilla (fidelbreamontilla@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fidelbrea.clientealarma;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class SettingLocalSettingsActivity extends AppCompatActivity {

    String confServerUrl;
    Integer confServerPort;
    Boolean confVibrate;
    Integer confVibrateDuration;

    private EditText serverUrlTextInput;
    private EditText serverPortTextInput;
    private TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(SettingLocalSettingsActivity.this, WelcomeActivity.class);
            startActivity(intent);
            SettingLocalSettingsActivity.this.finish();
        }

        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        confServerUrl = myPreferences.getString("URL_SERVER", "");
        confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
        confVibrate = myPreferences.getBoolean("VIBRATE?", true);
        confVibrateDuration = myPreferences.getInt("VIBRATE_DURATION", 50);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_local_settings);

        final int mUIFlag = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(mUIFlag);

        ((TextView) findViewById(R.id.pageTitle)).setText(getString(R.string.local_settings));

        Button applyButton = findViewById(R.id.applyButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        serverUrlTextInput = findViewById(R.id.serverUrlTextInput);
        serverUrlTextInput.setText(confServerUrl);
        serverPortTextInput = findViewById(R.id.serverPortTextInput);
        serverPortTextInput.setText(confServerPort.toString());
        errorView = findViewById(R.id.errorView);

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_pressed);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        vibrate();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    confServerUrl = serverUrlTextInput.getText().toString();
                                    confServerPort = Integer.parseInt(serverPortTextInput.getText().toString());
                                    CallHandler callHandler = new CallHandler();
                                    Client client = new Client(confServerUrl, confServerPort, callHandler);
                                    ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                    client.close();

                                    SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor myEditor = myPreferences.edit();
                                    myEditor.putString("URL_SERVER", confServerUrl);
                                    myEditor.putInt("PORT_SERVER", confServerPort);
                                    myEditor.putBoolean("VIBRATE?", true);
                                    myEditor.putInt("VIBRATE_DURATION", 50);
                                    myEditor.apply();

                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.local_settings_updated), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });

                                    SettingLocalSettingsActivity.this.finish();
                                    overridePendingTransition(R.anim.right_in, R.anim.right_out);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.server_config_error), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                });
                view.startAnimation(animation);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_pressed);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        vibrate();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        SettingLocalSettingsActivity.this.finish();
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    }
                });
                view.startAnimation(animation);
            }
        });
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }
}
