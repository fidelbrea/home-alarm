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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lipermi.handler.CallHandler;
import lipermi.net.Client;
import rmi.ServicioRmiInt;


public class SignUpActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    Button signUpButton;
    EditText signUpEmailTextInput;
    EditText signUpPasswordTextInput;
    CheckBox agreementCheckBox;
    TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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

        mAuth = FirebaseAuth.getInstance();

        signUpEmailTextInput = findViewById(R.id.signUpEmailTextInput);
        signUpPasswordTextInput = findViewById(R.id.signUpPasswordTextInput);
        signUpButton = findViewById(R.id.signUpButton);
        agreementCheckBox = findViewById(R.id.agreementCheckbox);
        errorView = findViewById(R.id.signUpErrorView);


        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animationScale = AnimationUtils.loadAnimation(SignUpActivity.this, R.anim.button_pressed);
                signUpButton.startAnimation(animationScale);
                if (signUpEmailTextInput.getText().toString().contentEquals(getString(R.string.empty))) {
                    errorView.setText(R.string.error_email_empty);
                } else if (signUpPasswordTextInput.getText().toString().contentEquals("")) {
                    errorView.setText(R.string.error_password_empty);
                } else if (!agreementCheckBox.isChecked()) {
                    errorView.setText(R.string.error_agree_terms_and_conditions);
                } else {
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String confServerUrl = myPreferences.getString("URL_SERVER", "");
                                Integer confServerPort = myPreferences.getInt("PORT_SERVER", 28803);
                                if(!confServerUrl.equals("")) {
                                    CallHandler callHandler = new CallHandler();
                                    Client client = new Client(confServerUrl, confServerPort, callHandler);
                                    ServicioRmiInt servicioRmiInt = (ServicioRmiInt) client.getGlobal(ServicioRmiInt.class);
                                    boolean userAuthorized = servicioRmiInt.checkEmail(signUpEmailTextInput.getText().toString());
                                    client.close();
                                    if (userAuthorized) {
                                        mAuth.createUserWithEmailAndPassword(signUpEmailTextInput.getText().toString(), signUpPasswordTextInput.getText().toString()).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    try {
                                                        if (user != null)
                                                            user.sendEmailVerification()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                                                        SignUpActivity.this);
                                                                                alertDialogBuilder.setTitle(getString(R.string.verify_email_id));
                                                                                alertDialogBuilder
                                                                                        .setMessage(getString(R.string.verification_email_sent))
                                                                                        .setCancelable(false)
                                                                                        .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                                Intent signInIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                                                                                                SignUpActivity.this.finish();
                                                                                            }
                                                                                        });
                                                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                                                alertDialog.show();
                                                                            }
                                                                        }
                                                                    });
                                                    } catch (Exception e) {
                                                        errorView.setText(e.getMessage());
                                                    }
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, getString(R.string.error_authentication_failed), Toast.LENGTH_SHORT).show();
                                                    if (task.getException() != null) {
                                                        errorView.setText(task.getException().getMessage());
                                                    }
                                                }
                                            }
                                        });
                                    } else {
                                        errorView.setText(getString(R.string.error_email_not_authorized));
                                    }
                                } else {
                                    errorView.setText(getString(R.string.error_need_admin_sing_up));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }
}
