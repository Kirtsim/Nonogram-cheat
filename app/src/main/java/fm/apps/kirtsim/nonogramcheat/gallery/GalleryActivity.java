package fm.apps.kirtsim.nonogramcheat.gallery;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

import fm.apps.kirtsim.nonogramcheat.MainActivity;
import fm.apps.kirtsim.nonogramcheat.R;

public class GalleryActivity extends AppCompatActivity implements GalleryListFragment.GalleryFragmentListener ,
                                                                  SlideShowFragment.SlideShowFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_gallery, new GalleryListFragment(), GalleryListFragment.TAG)
                    .commit();
        }
        MainActivity.clearGlideMemory = true;
    }

    @Override
    public void onPuzzleClicked(ArrayList<PuzzleImage> puzzles, int position) {
        SlideShowFragment fragment = SlideShowFragment.newInstance(puzzles, position);
        FragmentTransaction txn = getSupportFragmentManager().beginTransaction();
        txn.replace(R.id.activity_gallery, fragment, SlideShowFragment.TAG);
        txn.addToBackStack(null);
        txn.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                    getSupportFragmentManager().popBackStack();
                    return true;
                }
                break;
            case R.id.home_menu_item:
                startActivity(MainActivity.createHomeIntent(this));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPresentationFinished() {
        getSupportFragmentManager().popBackStack();
    }
}
