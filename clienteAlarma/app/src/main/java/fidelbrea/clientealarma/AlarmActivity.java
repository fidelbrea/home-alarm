package fidelbrea.clientealarma;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.MenuItem;
import fidelbrea.clientealarma.menuitem.listener.RecyclerItemClickListener;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verify if user is signed-in correctly
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(AlarmActivity.this, WelcomeActivity.class);
            startActivity(intent);
            AlarmActivity.this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        txtMessage.setText(getString(R.string.alarm));

        AdapterMenuItem adapter = new AdapterMenuItem(this);
        adapter.add(new MenuItem(getString(R.string.arm_alarm), getDrawable(R.drawable.ic_alarm_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(getString(R.string.disarm_alarm), getDrawable(R.drawable.ic_alarm_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(getString(R.string.back), getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));

        LinearLayoutManager l = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.rvButtonApp);
        recyclerView.setLayoutManager(l);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_pressed);
                        animation.setAnimationListener(new Animation.AnimationListener(){
                            @Override
                            public void onAnimationStart(Animation animation){
                                vibrate();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation){}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.arm_alarm))) {
                                    AlertDialog dialog = new AlertDialog.Builder(AlarmActivity.this)
                                    .setMessage(getString(R.string.confirm_arm))
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    new Thread(new Runnable() {
                                                        public void run() {
                                                            try {
                                                                CallHandler callHandler = new CallHandler();
                                                                Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                                                ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                                                servicioRmiInt.armAlarm();
                                                                client.close();
                                                                runOnUiThread(new Runnable() {
                                                                    public void run() {
                                                                        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.alarm_armed), Toast.LENGTH_LONG);
                                                                        toast.show();
                                                                    }
                                                                });
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).start();
                                                }
                                            })
                                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // User cancelled the dialog
                                                }
                                            }).create();
                                    dialog.show();
                                    final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    button.setEnabled(false);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable(){
                                        @Override
                                        public void run() {
                                            button.setEnabled(true);
                                        }}, 3000);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.disarm_alarm))) {
                                    AlertDialog dialog = new AlertDialog.Builder(AlarmActivity.this)
                                            .setMessage(getString(R.string.confirm_disarm))
                                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    new Thread(new Runnable() {
                                                        public void run() {
                                                            try {
                                                                CallHandler callHandler = new CallHandler();
                                                                Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                                                ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                                                servicioRmiInt.disarmAlarm();
                                                                client.close();
                                                                runOnUiThread(new Runnable() {
                                                                    public void run() {
                                                                        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.alarm_disarmed), Toast.LENGTH_LONG);
                                                                        toast.show();
                                                                    }
                                                                });
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }).start();
                                                }
                                            })
                                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // User cancelled the dialog
                                                }
                                            }).create();
                                    dialog.show();
                                    final Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    button.setEnabled(false);
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable(){
                                        @Override
                                        public void run() {
                                            button.setEnabled(true);
                                        }}, 3000);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.back))) {
                                    AlarmActivity.this.finish();
                                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                                }
                            }
                        });
                        view.startAnimation(animation);
                    }

                    @Override public void onLongItemClick(View view, int position) {}
                })
        );


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
