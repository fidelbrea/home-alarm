package fidelbrea.clientealarma;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.listener.RecyclerItemClickListener;
import fidelbrea.clientealarma.switchitem.AdapterSwitchItem;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class SettingUserActivity extends AppCompatActivity {

    private String alias;
    private BroadcastReceiver broadcastReceiver;
    private boolean waitingForCode;
    private boolean waitingForTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(SettingUserActivity.this, WelcomeActivity.class);
            startActivity(intent);
            SettingUserActivity.this.finish();
        }

        try {
            alias = getIntent().getExtras().get("alias").toString();
        }catch (Exception e){
            e.printStackTrace();
            SettingUserActivity.this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.activity_user);
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

        ObjectAnimator colorAnimCode = ObjectAnimator.ofInt(
                (TextView)findViewById(R.id.textCodeTitle),
                "textColor",
                Color.BLACK,
                Color.RED);
        colorAnimCode.setEvaluator(new ArgbEvaluator());
        colorAnimCode.setDuration(600);
        colorAnimCode.setEvaluator(new ArgbEvaluator());
        colorAnimCode.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimCode.setRepeatMode(ValueAnimator.REVERSE);

        ObjectAnimator colorAnimTag = ObjectAnimator.ofInt(
                (TextView)findViewById(R.id.textTagTitle),
                "textColor",
                Color.BLACK,
                Color.RED);
        colorAnimTag.setEvaluator(new ArgbEvaluator());
        colorAnimTag.setDuration(600);
        colorAnimTag.setEvaluator(new ArgbEvaluator());
        colorAnimTag.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimTag.setRepeatMode(ValueAnimator.REVERSE);

        TextView pageTitle = findViewById(R.id.pageTitle);
        String pageTitleText = getString(R.string.settings) + " " + getString(R.string.users);
        pageTitle.setText(pageTitleText);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent.getAction().equals("CODE")) {
                        if(waitingForCode) {
                            waitingForCode = false;
                            colorAnimCode.pause();
                            ((TextView) findViewById(R.id.textCodeTitle)).setTextColor(Color.BLACK);
                            ((TextView) findViewById(R.id.textCode)).setText(intent.getExtras().get("code").toString());
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        CallHandler callHandler = new CallHandler();
                                        Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                        ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                        servicioRmiInt.updateUserCode(((TextView) findViewById(R.id.textEmail)).getText().toString(), intent.getExtras().get("code").toString());
                                        client.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                    if (intent.getAction().equals("TAG")) {
                        if(waitingForTag) {
                            waitingForTag = false;
                            colorAnimTag.pause();
                            ((TextView) findViewById(R.id.textTagTitle)).setTextColor(Color.BLACK);
                            ((TextView) findViewById(R.id.textTag)).setText(intent.getExtras().get("tag").toString());
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        CallHandler callHandler = new CallHandler();
                                        Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                        ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                        servicioRmiInt.updateUserTag(((TextView) findViewById(R.id.textEmail)).getText().toString(), intent.getExtras().get("tag").toString());
                                        client.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        waitingForCode = false;
        waitingForTag = false;
        IntentFilter filter = new IntentFilter("CODE");
        filter.addAction("TAG");
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver((broadcastReceiver), filter);

        AdapterSwitchItem adapterSwitches = new AdapterSwitchItem(this);
        AdapterMenuItem adapterButtons = new AdapterMenuItem(this);

        // get sensor info and put it into screen
        AsyncTaskGetUser asyncTaskGetUser = new AsyncTaskGetUser(this,
                adapterSwitches,
                adapterButtons,
                pageTitle,
                (TextView) findViewById(R.id.textEmail),
                (TextView) findViewById(R.id.textCode),
                (TextView) findViewById(R.id.textTag)
        );
        asyncTaskGetUser.execute(alias);

        LinearLayoutManager l1 = new LinearLayoutManager(this);
        RecyclerView recyclerViewSwitch = findViewById(R.id.rvSwitchApp);
        recyclerViewSwitch.setLayoutManager(l1);
        recyclerViewSwitch.setAdapter(adapterSwitches);
        recyclerViewSwitch.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerViewSwitch ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        vibrate();
                        adapterSwitches.getListButtonApp().get(position).getSwitchItem().setChecked(!adapterSwitches.getListButtonApp().get(position).getSwitchItem().isChecked());
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {}
                })
        );

        LinearLayoutManager l2 = new LinearLayoutManager(this);
        RecyclerView recyclerViewButton = findViewById(R.id.rvButtonApp);
        recyclerViewButton.setLayoutManager(l2);
        recyclerViewButton.setAdapter(adapterButtons);
        recyclerViewButton.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerViewButton ,new RecyclerItemClickListener.OnItemClickListener() {
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
                                if (adapterButtons.getListButtonApp().get(position).getText().equals(getString(R.string.back))) {
                                    SettingUserActivity.this.finish();
                                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                                }else if (adapterButtons.getListButtonApp().get(position).getText().equals(getString(R.string.edit_alias))){
                                    Intent intent = new Intent(getApplicationContext(), SettingUserModifyAliasActivity.class);
                                    intent.putExtra("alias", alias);
                                    startActivityForResult(intent, 1);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                }else if (adapterButtons.getListButtonApp().get(position).getText().equals(getString(R.string.modify_code))){
                                    waitingForCode = true;
                                    colorAnimCode.start();
                                    waitingForTag = false;
                                    colorAnimTag.pause();
                                    ((TextView) findViewById(R.id.textTagTitle)).setTextColor(Color.BLACK);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.advise_insert_code), Toast.LENGTH_LONG);
                                            toast.show();
                                        }
                                    });
                                }else if (adapterButtons.getListButtonApp().get(position).getText().equals(getString(R.string.modify_tag))){
                                    waitingForTag = true;
                                    colorAnimTag.start();
                                    waitingForCode = false;
                                    colorAnimCode.pause();
                                    ((TextView) findViewById(R.id.textCodeTitle)).setTextColor(Color.BLACK);
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.advise_aproach_tag), Toast.LENGTH_LONG);
                                            toast.show();
                                        }
                                    });
                                }
                            }
                        });
                        view.startAnimation(animation);
                    }

                    @Override public void onLongItemClick(View view, int position) {}
                })
        );


    }

    @Override
    protected void onDestroy() {
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            }catch(Exception e){
                // No problem.
            }
            broadcastReceiver = null;
        }
        super.onDestroy();
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

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    alias = data.getExtras().get("alias").toString();
                }catch (Exception e){
                    e.printStackTrace();
                    SettingUserActivity.this.finish();
                }
            }
        }
    }
}
