package fidelbrea.clientealarma;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowPictureActivity extends AppCompatActivity {

    private Drawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            startActivity(intent);
            ShowPictureActivity.this.finish();
        }

        try {
            Bundle extras = getIntent().getExtras();
            String fileName = extras.getString("picture_filename");
            File filePath = getFileStreamPath(fileName);
            drawable = Drawable.createFromPath(filePath.toString());
        } catch (Exception e) {
            e.printStackTrace();
            ShowPictureActivity.this.finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_show_picture);
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

        ImageView imageView = findViewById(R.id.imgCamera);
        imageView.setImageDrawable(drawable);

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
                        ShowPictureActivity.this.finish();
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    }
                });
                view.startAnimation(animation);
            }
        });

        PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(imageView);

    }

    private void vibrate() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }

}
