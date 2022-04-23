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

public class AddUserActivity extends AppCompatActivity {

    EditText emailTextInput;
    EditText aliasTextInput;
    Button acceptButton;
    Button cancelButton;
    TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verify if user is signed-in correctly
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(AddUserActivity.this, WelcomeActivity.class);
            startActivity(intent);
            AddUserActivity.this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_add_user);

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

        emailTextInput = findViewById(R.id.emailTextInput);
        aliasTextInput = findViewById(R.id.aliasTextInput);
        acceptButton = findViewById(R.id.acceptButton);
        cancelButton = findViewById(R.id.cancelButton);
        errorView = findViewById(R.id.errorView);

        acceptButton.setOnClickListener(new View.OnClickListener() {
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
                        if (emailTextInput.getText().toString().contentEquals("")) {
                            errorView.setText(getString(R.string.email) + " " + getString(R.string.error_empty_field));
                        } else if (emailTextInput.length()<6){
                            errorView.setText(getString(R.string.email) + " " + getString(R.string.error_length_short, 6));
                        } else if (aliasTextInput.getText().toString().contentEquals("")) {
                            errorView.setText(getString(R.string.alias) + " " + getString(R.string.error_empty_field));
                        } else if (aliasTextInput.length()<4 || aliasTextInput.length()>11){
                            errorView.setText(getString(R.string.alias) + " " + getString(R.string.error_length, 4, 11));
                        } else {
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        CallHandler callHandler = new CallHandler();
                                        Client client = new Client(getString(R.string.url_server), getResources().getInteger(R.integer.server_port), callHandler);
                                        ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                        if(servicioRmiInt.addUser(emailTextInput.getText().toString(), aliasTextInput.getText().toString())){
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.user_added), Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            });
                                            AddUserActivity.this.finish();
                                            overridePendingTransition(R.anim.right_in, R.anim.right_out);
                                        }else{
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.user_not_added), Toast.LENGTH_LONG);
                                                    toast.show();
                                                }
                                            });
                                        }
                                        client.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
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
                        AddUserActivity.this.finish();
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