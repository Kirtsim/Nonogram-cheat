package fm.apps.kirtsim.nonogramcheat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.bumptech.glide.Glide;

import fm.apps.kirtsim.nonogramcheat.gallery.GalleryActivity;
import fm.apps.kirtsim.nonogramcheat.user_input.PuzzleInputActivity;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
//    public static final String APP_NAME_LOWER_CASE = "nonogram_cheat";
    public static boolean clearGlideMemory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        Button solveButton = (Button) findViewById(R.id.solveButton);
        Context context = getApplication();
        solveButton.setOnClickListener(v -> startActivity(new Intent(context, PuzzleInputActivity.class)));
        Button galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(v -> startActivity(new Intent(context, GalleryActivity.class)));
    }



    public static Intent createHomeIntent(Context context) {
        if (context != null) {
            Intent homeIntent = new Intent(context, MainActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            return homeIntent;
        }
        return new Intent();
    }

    @Override
    public void onResume() {
        super.onResume();
        clearImageMemory();
    }

    private void clearImageMemory() {
        if(clearGlideMemory) {
            Glide.get(getApplicationContext()).clearMemory();
            clearGlideMemory = false;
            System.gc();
        }
    }

}
