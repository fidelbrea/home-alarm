package fidelbrea.clientealarma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class WelcomeActivity extends AppCompatActivity {
    public Integer REQUEST_EXIT = 9;
    public FirebaseAuth mAuth;
    public FirebaseUser currentUser;
    Button signUpButton;
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

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

        signInButton = findViewById(R.id.welcomeSignInButton);
        signUpButton = findViewById(R.id.welcomeSignUpButton);
        signInButton.setVisibility(INVISIBLE);
        signUpButton.setVisibility(INVISIBLE);

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    currentUser = mAuth.getCurrentUser();
                    if (currentUser != null && currentUser.isEmailVerified()) {
                        Intent MainActivity = new Intent(WelcomeActivity.this, MainMenuActivity.class);
                        startActivity(MainActivity);
                        WelcomeActivity.this.finish();
                    }
                }
            });
        }else{
            signInButton.setVisibility(VISIBLE);
            signUpButton.setVisibility(VISIBLE);
            Animation animationScale = AnimationUtils.loadAnimation(this,R.anim.button_beating);
            signInButton.startAnimation(animationScale);
        }

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animationScale = AnimationUtils.loadAnimation(WelcomeActivity.this,R.anim.button_pressed);
                signUpButton.startAnimation(animationScale);
                Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animationScale = AnimationUtils.loadAnimation(WelcomeActivity.this,R.anim.button_pressed);
                signInButton.startAnimation(animationScale);
                Intent signInIntent = new Intent(WelcomeActivity.this, SignInActivity.class);
                startActivityForResult(signInIntent, REQUEST_EXIT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EXIT) {
            if (resultCode == RESULT_OK) {
                super.onActivityResult(requestCode, resultCode, data);
                this.finish();
            }
        }
    }

}
