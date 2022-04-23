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
import fidelbrea.clientealarma.menuitem.MenuItem;
import fidelbrea.clientealarma.menuitem.listener.RecyclerItemClickListener;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(SettingsActivity.this, WelcomeActivity.class);
            startActivity(intent);
            SettingsActivity.this.finish();
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
        txtMessage.setText(getString(R.string.settings));

        AdapterMenuItem adapter = new AdapterMenuItem(this);
        adapter.add(new MenuItem(getString(R.string.users), getDrawable(R.drawable.ic_users_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(getString(R.string.sensors), getDrawable(R.drawable.ic_sensor_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(getString(R.string.cameras), getDrawable(R.drawable.ic_camera_mask), R.drawable.ic_button_icon_mask));
        adapter.add(new MenuItem(getString(R.string.back), getDrawable(R.drawable.ic_back_mask), R.drawable.ic_button_icon_mask));

        LinearLayoutManager l = new LinearLayoutManager(this);
        RecyclerView recyclerView = findViewById(R.id.rvButtonApp);
        recyclerView.setLayoutManager(l);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
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
                                if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.users))) {
                                    Intent intent = new Intent(getApplicationContext(), SettingUsersActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.sensors))) {
                                    Intent intent = new Intent(getApplicationContext(), SettingSensorsActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.cameras))) {
                                    Intent intent = new Intent(getApplicationContext(), SettingCamerasActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                } else if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.back))) {
                                    SettingsActivity.this.finish();
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
