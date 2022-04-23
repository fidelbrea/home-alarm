package fidelbrea.clientealarma;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import fidelbrea.clientealarma.menuitem.AdapterMenuItem;
import fidelbrea.clientealarma.menuitem.listener.RecyclerItemClickListener;
import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class PhotosActivity extends AppCompatActivity {

    private String cameraName;
    private String eventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verify if user is signed-in correctly
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(PhotosActivity.this, WelcomeActivity.class);
            startActivity(intent);
            PhotosActivity.this.finish();
        }

        Bundle extras = getIntent().getExtras();
        if(extras.size() != 2)
            PhotosActivity.this.finish();
        String cameraNameTemp = getIntent().getExtras().get("camera").toString();
        String eventNameTemp = getIntent().getExtras().get("event").toString();
        if(cameraNameTemp == null || cameraNameTemp.length() < 4 || eventNameTemp == null || eventNameTemp.length() < 4){
            PhotosActivity.this.finish();
        }
        cameraName = cameraNameTemp;
        eventName = eventNameTemp;
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
        txtMessage.setText(getString(R.string.cameras));

        AdapterMenuItem adapter = new AdapterMenuItem(this);

        // get camera events and pictures in camera directory and put items into adapter
        AsyncTaskListCams asyncTaskListCams = new AsyncTaskListCams(this, adapter);
        asyncTaskListCams.execute(getString(R.string.url_server), getString(R.string.back), cameraName, eventName);
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
                                if (adapter.getListButtonApp().get(position).getText().equals(getString(R.string.back))) {
                                    PhotosActivity.this.finish();
                                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                                } else if (adapter.getListButtonApp().get(position).getText().endsWith(".jpg")) {
                                    Bitmap bitmap = adapter.getListButtonApp().get(position).getPicture();
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                                    byte[] b = baos.toByteArray();
                                    String fileName = "temp.png";
                                    try {
                                        FileOutputStream fileOutStream = openFileOutput(fileName, MODE_PRIVATE);
                                        fileOutStream.write(b);
                                        fileOutStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    Intent intent = new Intent(getApplicationContext(), ShowPictureActivity.class);
                                    intent.putExtra("picture_filename", fileName);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.left_in, R.anim.left_out);
                                }
                            }
                        });
                        view.startAnimation(animation);
                    }

                    @Override public void onLongItemClick(View view, int position) {
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
                                if (!adapter.getListButtonApp().get(position).getText().equals(getString(R.string.shoot)) &&
                                        !adapter.getListButtonApp().get(position).getText().equals(getString(R.string.back))) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(PhotosActivity.this);
                                    if (adapter.getListButtonApp().get(position).getText().endsWith(".jpg")) {
                                        builder.setMessage(getString(R.string.confirm_delete, adapter.getListButtonApp().get(position).getText()))
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        new Thread(new Runnable() {
                                                            public void run() {
                                                                try {
                                                                    CallHandler callHandler = new CallHandler();
                                                                    Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                                                    ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                                                    boolean res = servicioRmiInt.deletePicture(cameraName, eventName, adapter.getListButtonApp().get(position).getText());
                                                                    client.close();
                                                                    if (res) {
                                                                        runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                adapter.remove(position);
                                                                            }
                                                                        });
                                                                    }
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
                                                });
                                    }
                                    builder.create();
                                    builder.show();
                                }
                            }
                        });
                        view.startAnimation(animation);
                    }
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
