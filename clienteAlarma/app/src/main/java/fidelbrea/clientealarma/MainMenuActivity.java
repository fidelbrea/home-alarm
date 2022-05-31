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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.listener.RecyclerItemClickListener;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class MainMenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BroadcastReceiver broadcastReceiver;
    String confServerUrl;
    Integer confServerPort;
    Boolean confVibrate;
    Integer confVibrateDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(MainMenuActivity.this, WelcomeActivity.class);
            startActivity(intent);
            MainMenuActivity.this.finish();
        }

        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        confServerUrl = myPreferences.getString("URL_SERVER", "");
        confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
        confVibrate = myPreferences.getBoolean("VIBRATE?", true);
        confVibrateDuration = myPreferences.getInt("VIBRATE_DURATION", 50);

        if(!confServerUrl.equals("")) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        CallHandler callHandler = new CallHandler();
                        Client client = new Client(confServerUrl, confServerPort, callHandler);
                        ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                        final boolean isServerEmailVerified = servicioRmiInt.checkEmail(currentUser.getEmail());
                        if (!isServerEmailVerified) {
                            currentUser.delete();
                            mAuth.signOut();
                            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                            startActivity(intent);
                            MainMenuActivity.this.finish();
                        }
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if(confServerUrl.equals("")) {
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            confServerUrl = myPreferences.getString("URL_SERVER", "");
            confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        CallHandler callHandler = new CallHandler();
                        Client client = new Client(confServerUrl, confServerPort, callHandler);
                        ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        final boolean isServerEmailVerified = servicioRmiInt.checkEmail(currentUser.getEmail());
                        if (!isServerEmailVerified) {
                            currentUser.delete();
                            mAuth.signOut();
                            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                            startActivity(intent);
                            MainMenuActivity.this.finish();
                        }
                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        setContentView(R.layout.activity_with_recyclerview);
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

        TextView txtMessage = findViewById(R.id.pageTitle);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent.getAction().equals("ALARM_STATE")) {
                        switch (Integer.parseInt(intent.getExtras().get("alarm_state").toString())) {
                            case 0:
                                txtMessage.setText(getString(R.string.state_disarmed));
                                txtMessage.setTextColor(Color.WHITE);
                                txtMessage.setBackgroundColor(Color.rgb(0x00, 0x57, 0x3F));
                                break;
                            case 3:
                                txtMessage.setText(getString(R.string.state_armed));
                                txtMessage.setTextColor(Color.GREEN);
                                txtMessage.setBackgroundColor(Color.rgb(0x00, 0x57, 0x3F));
                                break;
                            case 6:
                                txtMessage.setText(getString(R.string.state_triggered));
                                txtMessage.setTextColor(Color.YELLOW);
                                txtMessage.setBackgroundColor(Color.RED);
                                break;
                            default:
                                txtMessage.setText(getString(R.string.state_unknown));
                                txtMessage.setTextColor(Color.WHITE);
                                txtMessage.setBackgroundColor(Color.rgb(0x00, 0x57, 0x3F));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        IntentFilter filter = new IntentFilter("ALARM_STATE");
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver), filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // If APP level >= 26 (Android 8.0) create a notification channel
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.default_notification_channel_id), getString(R.string.default_notification_channel_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            notificationChannel.setDescription(getString(R.string.default_notification_channel_description));
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            notificationChannel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getApplicationContext().getPackageName() + "/" + R.raw.sirena), att);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{300, 300});
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }

        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.default_notification_channel_name))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful())
                            Toast.makeText(MainMenuActivity.this, getString(R.string.msg_subscribe_failed), Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed. " + task.getException().toString());
                            return;
                        }
                        String token = task.getResult();
                        FirebaseUser currentUser = mAuth.getCurrentUser();
                        if (currentUser != null && currentUser.isEmailVerified()) {
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        CallHandler callHandler = new CallHandler();
                                        Client client = new Client(confServerUrl, confServerPort, callHandler);
                                        ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                        servicioRmiInt.registerUser(mAuth.getCurrentUser().getEmail(), token);
                                        client.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        } else {
                            Intent intent = new Intent(MainMenuActivity.this, WelcomeActivity.class);
                            startActivity(intent);
                            MainMenuActivity.this.finish();
                        }
                    }
                });


        new Thread(new Runnable() {
            public void run() {
                try {
                    CallHandler callHandler = new CallHandler();
                    Client client = new Client(confServerUrl, confServerPort, callHandler);
                    ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                    switch (servicioRmiInt.getAlarmState()) {
                        case 0:
                            txtMessage.setText(getString(R.string.state_disarmed));
                            txtMessage.setTextColor(Color.WHITE);
                            txtMessage.setBackgroundColor(Color.rgb(0x00, 0x57, 0x3F));
                            break;
                        case 3:
                            txtMessage.setText(getString(R.string.state_armed));
                            txtMessage.setTextColor(Color.GREEN);
                            txtMessage.setBackgroundColor(Color.rgb(0x00, 0x57, 0x3F));
                            break;
                        case 6:
                            txtMessage.setText(getString(R.string.state_triggered));
                            txtMessage.setTextColor(Color.YELLOW);
                            txtMessage.setBackgroundColor(Color.RED);
                            break;
                        default:
                            txtMessage.setText(getString(R.string.state_unknown));
                            txtMessage.setTextColor(Color.WHITE);
                            txtMessage.setBackgroundColor(Color.rgb(0x00, 0x57, 0x3F));
                    }
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        AdapterMenuItem adapter = new AdapterMenuItem(this);
        AsyncTaskCheckIfAdministratorUser asyncTaskCheckIfAdministratorUser = new AsyncTaskCheckIfAdministratorUser(this, adapter);
        asyncTaskCheckIfAdministratorUser.execute(mAuth.getCurrentUser().getEmail());
        LinearLayoutManager l = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.rvButtonApp);
        recyclerView.setLayoutManager(l);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
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
                                if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.alarm))) {
                                    Intent intent = new Intent(getApplicationContext(), AlarmActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.cameras))) {
                                    Intent intent = new Intent(getApplicationContext(), CamerasActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.log))) {
                                    Intent intent = new Intent(getApplicationContext(), LogActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.settings))) {
                                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.exit))) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
                                    builder.setMessage(getApplicationContext().getString(R.string.confirm_exit))
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    FirebaseAuth.getInstance().signOut();
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                }
                                            });
                                    builder.show();
                                }
                            }
                        });
                        view.startAnimation(animation);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

    }

    @Override
    protected void onResume() {
        super.onResume();
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
    }

    private void vibrate() {
        if(confVibrate) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(confVibrateDuration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(confVibrateDuration);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
            }
            broadcastReceiver = null;
        }
        super.onDestroy();
    }

}