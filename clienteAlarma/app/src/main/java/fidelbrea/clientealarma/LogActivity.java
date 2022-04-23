package fidelbrea.clientealarma;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import fidelbrea.clientealarma.logentryitem.AdapterLogEntryItem;
import fidelbrea.clientealarma.logentryitem.LogEntryItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verify if user is signed-in correctly
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(LogActivity.this, WelcomeActivity.class);
            startActivity(intent);
            LogActivity.this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.activity_log);
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
        txtMessage.setText(getString(R.string.log));

        AdapterLogEntryItem adapter = new AdapterLogEntryItem(this);
        new Thread(new Runnable() {
            public void run() {
                try {
                    CallHandler callHandler = new CallHandler();
                    Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                    ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                    JSONObject jsonRes = new JSONObject(servicioRmiInt.getLastEvents(30));
                    if(jsonRes.has("events")){
                        JSONArray aEvents = (JSONArray) jsonRes.getJSONArray("events");
                        HashMap<Integer, LogEntryItem> eventMap = new HashMap<>();
                        for(int i=0; i<aEvents.length(); i++){
                            JSONObject jsonEvent = new JSONObject(aEvents.getString(i));
                            eventMap.put(jsonEvent.getInt("id"), new LogEntryItem(jsonEvent.getString("timestamp"), jsonEvent.getString("event")));
                        }
                        runOnUiThread(new Runnable() {
                            public void run() {
                                for(int i=0; i<eventMap.size(); i++){
                                    adapter.add((LogEntryItem) eventMap.get(i));
                                }
                            }
                        });
                    }
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        LinearLayoutManager l = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.rvButtonApp);
        recyclerView.setLayoutManager(l);
        recyclerView.setAdapter(adapter);

        ImageView imgBack = findViewById(R.id.imgBack);
        imgBack.setOnClickListener(new View.OnClickListener() {
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
                        LogActivity.this.finish();
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    }
                });
                view.startAnimation(animation);
            }
        });

    }

    private void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }

}
