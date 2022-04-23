package firebase;

/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import fidelbrea.clientealarma.R;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 *
 * <service android:name="firebase.MyFirebaseMessagingService" />
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when message is received.
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // https://firebase.google.com/docs/cloud-messaging/concept-options
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage != null) {
            System.out.println("onMessageReceived::From: " + remoteMessage.getFrom());
            Map<String, String> datos = remoteMessage.getData();
            if (remoteMessage.getData().size() > 0) {
                if(datos.get("alarm_state")!=null){
                    Intent intent = new Intent("ALARM_STATE");
                    intent.putExtra("alarm_state", datos.get("alarm_state"));
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                }
                if(datos.get("code")!=null){
                    Intent intent = new Intent("CODE");
                    intent.putExtra("code", datos.get("code"));
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                }
                if(datos.get("tag")!=null){
                    Intent intent = new Intent("TAG");
                    intent.putExtra("tag", datos.get("tag"));
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
                }
            }
            if (remoteMessage.getNotification() != null) {
                sendNotification(remoteMessage.getNotification().getBody());
            }
        }
    }

    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     *     2.A) App is restored to a new device
     *     2.B) User uninstalls/reinstalls the app
     *     2.C) User clears app data
     */
    @Override
    public void onNewToken(String token) {
        System.out.println("onNewToken::Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed. " + task.getException().toString());
                            return;
                        }
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    CallHandler callHandler = new CallHandler();
                                    Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                    ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    servicioRmiInt.registerUser(mAuth.getCurrentUser().getEmail(), token);
                                    client.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                });
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/res/raw/" + R.raw.sirena);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                        .setSmallIcon(R.drawable.alarm)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(soundUri);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    getString(R.string.default_notification_channel_id),
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(0, notificationBuilder.build());
    }
}