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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.listener.RecyclerItemClickListener;
import fidelbrea.clientealarma.switchitem.AdapterSwitchItem;

public class SettingSensorActivity extends AppCompatActivity {

    private String alias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(SettingSensorActivity.this, WelcomeActivity.class);
            startActivity(intent);
            SettingSensorActivity.this.finish();
        }

        try {
            String aliasTemp = getIntent().getExtras().get("alias").toString();
            if (aliasTemp == null)
                SettingSensorActivity.this.finish();
            alias = aliasTemp;
        }catch (Exception e){
            e.printStackTrace();
            SettingSensorActivity.this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setContentView(R.layout.activity_sensor);
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

        AdapterSwitchItem adapterSwitches = new AdapterSwitchItem(this);
        AdapterMenuItem adapterButtons = new AdapterMenuItem(this);

        // get sensor info and put it into screen
        AsyncTaskGetSensor asyncTaskGetSensor = new AsyncTaskGetSensor(this, adapterSwitches, adapterButtons, (TextView) findViewById(R.id.pageTitle));
        asyncTaskGetSensor.execute(getString(R.string.url_server), alias);

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
                                    // ToDo launch AsyncTask to synchronize sensor data with server
                                    SettingSensorActivity.this.finish();
                                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                                }else if (adapterButtons.getListButtonApp().get(position).getText().equals(getString(R.string.edit_alias))){
                                    Intent intent = new Intent(getApplicationContext(), SettingSensorModifyAliasActivity.class);
                                    intent.putExtra("alias", alias);
                                    startActivityForResult(intent, 1);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                }else if (adapterButtons.getListButtonApp().get(position).getText().equals(getString(R.string.associate_with_cams))) {
                                    Intent intent = new Intent(SettingSensorActivity.this, SettingSensorCamsActivity.class);
                                    intent.putExtra("alias", alias);
                                    startActivity(intent);
                                    SettingSensorActivity.this.finish();
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
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


    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                try {
                    String aliasTemp = data.getExtras().get("alias").toString();
                    if (aliasTemp == null)
                        SettingSensorActivity.this.finish();
                    alias = aliasTemp;
                }catch (Exception e){
                    e.printStackTrace();
                    SettingSensorActivity.this.finish();
                }
            }
        }
    }
}
