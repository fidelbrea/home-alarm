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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;

public class SignInActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    EditText emailTextInput;
    EditText passwordTextInput;
    Button signInButton;
    Button forgotPasswordButton;
    Button sendVerifyMailAgainButton;
    TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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

        emailTextInput = findViewById(R.id.signInEmailTextInput);
        passwordTextInput = findViewById(R.id.signInPasswordTextInput);
        signInButton = findViewById(R.id.signInButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        sendVerifyMailAgainButton = findViewById(R.id.verifyEmailAgainButton);
        errorView = findViewById(R.id.signInErrorView);

        sendVerifyMailAgainButton.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animationScale = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.button_pressed);
                signInButton.startAnimation(animationScale);
                if (emailTextInput.getText().toString().contentEquals("")) {
                    errorView.setText(R.string.error_email_empty);
                } else if (passwordTextInput.getText().toString().contentEquals("")) {
                    errorView.setText(R.string.error_password_empty);
                } else {
                    mAuth.signInWithEmailAndPassword(emailTextInput.getText().toString(), passwordTextInput.getText().toString())
                            .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if (user != null) {
                                            if (user.isEmailVerified()) {
                                                new Thread(new Runnable() {
                                                    public void run() {
                                                        try {
                                                            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                                            String confServerUrl = myPreferences.getString("URL_SERVER", "");
                                                            Integer confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
                                                            boolean userAuthorized = false;
                                                            if(confServerUrl.equals("")) {
                                                                userAuthorized = true;
                                                            }else{
                                                                CallHandler callHandler = new CallHandler();
                                                                Client client = new Client(confServerUrl, confServerPort, callHandler);
                                                                ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                                                userAuthorized = servicioRmiInt.checkEmail(user.getEmail());
                                                                client.close();
                                                            }
                                                            if (userAuthorized) {
                                                                Intent intent = new Intent(SignInActivity.this, MainMenuActivity.class);
                                                                setResult(RESULT_OK, null);
                                                                startActivity(intent);
                                                                SignInActivity.this.finish();
                                                            } else {
                                                                user.delete();
                                                                mAuth.signOut();
                                                                passwordTextInput.setText("");
                                                                errorView.setText(R.string.error_authentication_failed);
                                                                sendVerifyMailAgainButton.setVisibility(View.INVISIBLE);
                                                            }
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).start();
                                            } else {
                                                passwordTextInput.setText("");
                                                sendVerifyMailAgainButton.setVisibility(View.VISIBLE);
                                                errorView.setText(R.string.verify_email_id);
                                            }
                                        }

                                    } else {
                                        Toast.makeText(SignInActivity.this, getString(R.string.error_authentication_failed), Toast.LENGTH_SHORT).show();
                                        if (task.getException() != null) {
                                            errorView.setText(task.getException().getMessage());
                                        }

                                    }

                                }
                            });
                }
            }
        });

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animationScale = AnimationUtils.loadAnimation(SignInActivity.this, R.anim.button_pressed);
                forgotPasswordButton.startAnimation(animationScale);
                Intent forgotPasswordActivity = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(forgotPasswordActivity);
                SignInActivity.this.finish();
            }
        });

    }
}
